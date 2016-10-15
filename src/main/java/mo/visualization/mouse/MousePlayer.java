package mo.visualization.mouse;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
import mo.visualization.Playable;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class MousePlayer implements Playable {

    private double speed = 1;
    private long currentTime, start, end;
    private boolean isPlaying = false;

    private RandomAccessFile file;

    private TestPane pane;

    private MouseEvent currentEvent;

    private Thread thread;

    private static final Logger logger = Logger.getLogger(MousePlayer.class.getName());
    private long timeToSleep;

    public MousePlayer(File file) {
        try {
            this.file = new RandomAccessFile(file, "r");

            String line = this.file.readLine();
            List<Rectangle> bounds = parseScreens(line);

            ReversedLinesFileReader rev = new ReversedLinesFileReader(file, Charset.defaultCharset());
            String lastLine = null;
            do {
                lastLine = rev.readLine();
                if (lastLine == null) {
                    break;
                }
            } while (lastLine.trim().isEmpty());
            rev.close();

            MouseEvent lastEvent = parseEventFromLine(lastLine);
            if (lastEvent != null) {
                end = lastEvent.time;
            }

            currentEvent = readNextEventFromFile();
            if (currentEvent != null) {
                start = currentEvent.time;
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pane = new TestPane(bounds);
                    DockableElement e = new DockableElement();
                    e.add(pane);
                    DockablesRegistry.getInstance().addAppWideDockable(e);

                    System.out.println(start + " " + end + " " + currentEvent);
                }
            });

        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private MouseEvent readNextEventFromFile() {
        String line;
        try {
            line = file.readLine();
            if (line != null) {
                return parseEventFromLine(line);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void setSpeed(double factor) {
        speed = factor;
    }

    @Override
    public void pause() {
        isPlaying = false;
    }

    @Override
    public void seek(long desiredMillis) {
        if (desiredMillis < start) {
            seek(start);
            timeToSleep = start - desiredMillis;
            if (isPlaying) {
                play();
            }
            return;
        }

        if (desiredMillis > end) {
            seek(end);
            isPlaying = false;
            return;
        }

        boolean playing = isPlaying;

        if (isPlaying) {
            isPlaying = false;
        }

        MouseEvent event = currentEvent;

        if (desiredMillis < currentEvent.time) {
            try {
                file.seek(0);
                file.readLine(); // first line contains screens

                event = readNextEventFromFile();

            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        long marker;
        try {
            marker = file.getFilePointer();

            MouseEvent next = readNextEventFromFile();
            if (next == null) {
                return;
            }

            while (!(next.time > desiredMillis)) {
                event = next;

                marker = file.getFilePointer();
                next = readNextEventFromFile();
            }

            file.seek(marker);
            currentEvent = event;
            timeToSleep = desiredMillis - currentEvent.time;
            //return;

            isPlaying = playing;
            if (isPlaying) {
                play();
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void play() {
        isPlaying = true;

        thread = new Thread() {
            @Override
            public void run() {
                if (timeToSleep > 0) {
                    try {
                        sleep(timeToSleep);
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
                timeToSleep = 0;

                MouseEvent nextEvent = null;
                while (isPlaying) {
                    nextEvent = readNextEventFromFile();
                    if (currentEvent == null || nextEvent == null) {
                        isPlaying = false;
                        interrupt();

                        System.out.println("No more data in file");
                        return;
                    }
                    
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            pane.display(currentEvent);
                        }
                    });
                    
                    long sleepTime = (long) ((nextEvent.time - currentEvent.time) / speed);
                    currentEvent = nextEvent;
                    if (sleepTime > 0) {
                        try {
                            sleep(sleepTime);
                        } catch (InterruptedException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

        };
        thread.start();
    }

    private MouseEvent parseEventFromLine(String line) {
        if (line != null && line.contains(",")) {
            MouseEvent e = new MouseEvent();
            int index = line.indexOf(",");
            String when = line.substring(0, index);
            long time = Long.parseLong(when);
            index++;
            String eventType = line.substring(index, line.indexOf(",", index++));
            switch (eventType) {
                case ("NATIVE_MOUSE_CLICKED"):
                    e.type = MouseEventType.NATIVE_MOUSE_CLICKED;
                    break;

                case ("NATIVE_MOUSE_PRESSED"):
                    e.type = MouseEventType.NATIVE_MOUSE_PRESSED;
                    break;

                case ("NATIVE_MOUSE_RELEASED"):
                    e.type = MouseEventType.NATIVE_MOUSE_RELEASED;
                    break;

                case ("NATIVE_MOUSE_MOVED"):
                    e.type = MouseEventType.NATIVE_MOUSE_MOVED;
                    break;

                case ("NATIVE_MOUSE_DRAGGED"):
                    e.type = MouseEventType.NATIVE_MOUSE_DRAGGED;
                    break;

                case ("NATIVE_MOUSE_WHEEL"):
                    e.type = MouseEventType.NATIVE_MOUSE_WHEEL;
                    break;

                default:
                    e.type = null;
                    break;
            }
            index += eventType.length() + 1;
            String xStr = line.substring(index, line.indexOf(",", index++));
            index += xStr.length();

            String yStr = line.substring(index, line.indexOf("),", index++));

            index += yStr.length() + 1;

            String button = line.substring(line.indexOf("=", index) + 1, line.indexOf(",", index));
            e.x = Integer.parseInt(xStr);
            e.y = Integer.parseInt(yStr);
            e.time = time;
            e.button = Integer.parseInt(button);
            return e;
        }
        return null;
    }

    enum MouseEventType {
        NATIVE_MOUSE_CLICKED,
        NATIVE_MOUSE_PRESSED,
        NATIVE_MOUSE_RELEASED,
        NATIVE_MOUSE_MOVED,
        NATIVE_MOUSE_DRAGGED,
        NATIVE_MOUSE_WHEEL
    };

    class MouseEvent {

        int x, y, clickCount, button;
        long time;
        MouseEventType type;

        @Override
        public String toString() {
            return time + " (" + x + "," + y + ") " + type.name() + " " + button;
        }

    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        return end;
    }

    private static List<Rectangle> parseScreens(String line) {
        ArrayList<Rectangle> screens = new ArrayList<>();
        if (line.contains(";")) {
            String screensStrs[] = line.split(";");
            for (String screenStr : screensStrs) {
                Rectangle r = parseRectangle(screenStr);
                if (r != null) {
                    screens.add(r);
                }
            }
        } else {
            Rectangle r = parseRectangle(line);
            if (r != null) {
                screens.add(r);
            }
        }
        return screens;
    }

    private static Rectangle parseRectangle(String str) {
        int x, y, w, h, i;

        if (str.contains("x=")) {
            i = str.indexOf(",");
            x = Integer.parseInt(str.substring(str.indexOf("x=") + 2, i));
        } else {
            return null;
        }

        if (str.contains("y=")) {
            i = str.indexOf(",", i + 1);
            y = Integer.parseInt(str.substring(str.indexOf("y=") + 2, i));
        } else {
            return null;
        }

        if (str.contains("width=")) {
            i = str.indexOf(",", i + 1);
            w = Integer.parseInt(str.substring(str.indexOf("width=") + 6, i));
        } else {
            return null;
        }

        if (str.contains("height=")) {
            h = Integer.parseInt(str.substring(str.indexOf("height=") + 7, str.length()));
        } else {
            return null;
        }
        return new Rectangle(x, y, w, h);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println();
        File f = new File("C:\\Users\\Celso\\Desktop\\ejemplo\\participant-00\\capture\\2016-10-03_17.04.05.387_m.txt");
        MousePlayer p = new MousePlayer(f);

        JFrame fr = new JFrame();

        fr.add(p.pane);
        fr.setPreferredSize(new Dimension(400, 400));
        fr.setSize(400, 400);
        fr.setDefaultCloseOperation(EXIT_ON_CLOSE);
        //fr.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                fr.setVisible(true);
            }
        });

        //p.seek(1475283430008L);
        System.out.println(p.currentEvent.time);
        p.play();

    }

    public static class TestPane extends JPanel {

        private double scale;
        private final List<Rectangle> screenBounds;

        private Point virtualPoint;
        private Point screenPoint;

        private Rectangle virtualBounds = new Rectangle(0, 0, 0, 0);

        public TestPane(List<Rectangle> screens) {
            screenBounds = screens;
            for (Rectangle screen : screens) {
                virtualBounds.add(screen);
            }
        }

        @Override
        public void invalidate() {
            super.invalidate();
            scale = getScaleFactorToFit(virtualBounds.getSize(), getSize());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 200);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int xOffset = 0;
            int yOffset = 0;
            List<Rectangle> scaledBounds = new ArrayList<>(screenBounds.size());
            for (Rectangle bounds : screenBounds) {
                bounds = scale(bounds);
                scaledBounds.add(bounds);
                if (bounds.x < xOffset) {
                    xOffset = bounds.x;
                }
                if (bounds.y < yOffset) {
                    yOffset = bounds.y;
                }
            }
            if (xOffset < 0) {
                xOffset *= -1;
            }
            if (yOffset < 0) {
                yOffset *= -1;
            }

            Graphics2D g2d = (Graphics2D) g.create();
            for (Rectangle bounds : scaledBounds) {
                bounds.x += xOffset;
                bounds.y += yOffset;
                g2d.setColor(Color.DARK_GRAY);
                g2d.fill(bounds);
                g2d.setColor(Color.GRAY);
                g2d.draw(bounds);
            }

            FontMetrics fm = g2d.getFontMetrics();

            g2d.setColor(Color.WHITE);
            if (screenPoint != null) {
                int x = 0;
                int y = fm.getAscent();

                g2d.drawString(screenPoint.x + "," + screenPoint.y, x, y);
                screenPoint.x += xOffset;
                screenPoint.y += yOffset;
                screenPoint.x *= scale;
                screenPoint.y *= scale;
                g2d.fillOval(screenPoint.x - 2, screenPoint.y - 2, 4, 4);
            }

            if (virtualPoint != null) {
                int x = 0;
                int y = fm.getAscent() + fm.getHeight();

                g2d.drawString(virtualPoint.toString(), x, y);
            }

            g2d.dispose();
        }

        protected Rectangle scale(Rectangle bounds) {
            Rectangle scaled = new Rectangle(bounds);
            scaled.x *= scale;
            scaled.y *= scale;
            scaled.width *= scale;
            scaled.height *= scale;
            return scaled;
        }

        private void display(MouseEvent event) {
            screenPoint = new Point(event.x, event.y);
            repaint();
        }

    }

    public static double getScaleFactorToFit(Dimension original, Dimension toFit) {
        double dScale = 1d;
        if (original != null && toFit != null) {
            double dScaleWidth = getScaleFactor(original.width, toFit.width);
            double dScaleHeight = getScaleFactor(original.height, toFit.height);
            dScale = Math.min(dScaleHeight, dScaleWidth);
        }
        return dScale;
    }

    public static double getScaleFactor(int iMasterSize, int iTargetSize) {
        double dScale = 1;
        dScale = (double) iTargetSize / (double) iMasterSize;
        return dScale;
    }
}
