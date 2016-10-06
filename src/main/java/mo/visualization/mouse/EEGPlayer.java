package mo.visualization.mouse;

import com.theeyetribe.clientsdk.response.TrackerGetResponse;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import mo.visualization.Playable;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class EEGPlayer implements Playable {

    private double speed = 01;
    private long currentTime, start, end;
    private boolean isPlaying = false;

    private RandomAccessFile raf;

    private TestPane pane;

    private EEGData current;

    private static final Logger logger = Logger.getLogger(EEGPlayer.class.getName());

    public EEGPlayer(File file) {
        try {
            readLastTime(file);
//            EEGData last = parse
//            if (trackerRes.category.equals("tracker")) {
//                end = dateStringToMillis(trackerRes.values.frame.timeStampString);
//            }
//
             raf = new RandomAccessFile(file, "r");

        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } 
    }

    void readLastTime(File file) {
        try {
            ReversedLinesFileReader rev = new ReversedLinesFileReader(file, Charset.defaultCharset());
            String lastLine = null;
            do {
                lastLine = rev.readLine();
            } while (lastLine == null && lastLine.trim().isEmpty());
            rev.close();
            
            int index = lastLine.indexOf("#Time:");
            System.out.println(lastLine+ " " +index);
            
            end = Long.parseLong(
                    lastLine.substring(index+6, lastLine.indexOf(",", index)));
            end = end * 1000;
            
            double d = Double.parseDouble(lastLine.substring(lastLine.indexOf(",", index)+1, lastLine.lastIndexOf("#")));
            d /= 1000;
            System.out.println("end:"+end+" ddd:"+d);
            
            end = (long) (end+ d);
            System.out.println("end:"+end+" ddd:"+d);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(end);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR);
            int mins = calendar.get(Calendar.MINUTE);
            int secs = calendar.get(Calendar.SECOND);
            int millis = calendar.get(Calendar.MILLISECOND);
            
            System.out.println("end "+end + " " + year + " " + month + " " + day + " " + hour + " " + mins + " " + secs +" "+ millis);
            System.out.println(DateFormat.getDateInstance(DateFormat.LONG).format(end));
            System.out.println(Instant.ofEpochMilli(end).atOffset(ZoneOffset.ofHours(-4)));
        } catch (IOException ex) {
            Logger.getLogger(EEGPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static long dateStringToMillis(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date;
        try {
            date = dateFormat.parse(dateStr);
            return date.getTime();
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    int parseMeditation(String line) {
        return 0;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println();
        File f = new File("C:\\Users\\Celso\\Desktop\\log_2016_6_7_11_43_3.txt");

        EEGPlayer p = new EEGPlayer(f);

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
    public void seek(long millis) {
        boolean playing = isPlaying;

        if (isPlaying) {
            isPlaying = false;
        }

//        if (millis < currentEvent.time) {
//            try {
//                raf.seek(0);
//                raf.readLine();
//                String line = raf.readLine();
//                if (line != null) {
//                    currentEvent = parseEventFromLine(line);
//                }
//                while (!(currentEvent.time <= millis)) {
//                    try {
//                        String line2 = raf.readLine();
//                        MouseEvent next;
//                        if (line2 != null) {
//                            next = parseEventFromLine(line);
//                            if (next.time < millis) {
//                                currentEvent = next;
//                            } else if (next.time >= millis) {
//                                break;
//                            }
//                        }
//                    } catch (IOException ex) {
//                        logger.log(Level.SEVERE, null, ex);
//                    }
//                }
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }
//        } else if (currentEvent.time < millis) {
//            System.out.println("mayor");
//            while (!(currentEvent.time > millis)) {
//                System.out.println("while");
//                try {
//                    String line = raf.readLine();
//                    MouseEvent next;
//                    if (line != null) {
//                        next = parseEventFromLine(line);
//                        if (next.time <= millis) {
//                            currentEvent = next;
//                        } else if (next.time > millis) {
//                            break;
//                        }
//                    }
//                } catch (IOException ex) {
//                    logger.log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//
//        isPlaying = playing;
    }

    @Override
    public void play() {
        isPlaying = true;
        EEGData next = null;
        while (isPlaying) {
//            try {
//                if (currentEvent == null) {
//                    String line = raf.readLine();
//                    if (line != null) {
//                        currentEvent = parseEventFromLine(line);
//                    }
//                }
//
//                String line2 = raf.readLine();
//                if (line2 != null) {
//                    nextEvent = parseEventFromLine(line2);
//                } else {
//                    System.exit(0);
//                }
//
//                pane.display(currentEvent);
//                long sleep = (long) ((nextEvent.time - currentEvent.time) / speed);
//                currentEvent = nextEvent;
//                try {
//                    Thread.sleep(sleep);
//                } catch (InterruptedException ex) {
//                    logger.log(Level.SEVERE, null, ex);
//                }
//
//            } catch (IOException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }

        }
    }

    class EEGData {

        long time;
        int poorSignal, delta, theta, alpha1, alpha2, beta1, beta2, gamma1, gamma2, attention, meditation;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
//            Timer timer = new Timer(20, new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
////                    PointerInfo pi = MouseInfo.getPointerInfo();
////
////                    Point mp = pi.getLocation();
////
////                    Rectangle bounds = getDeviceBounds(pi.getDevice());
////
////                    screenPoint = new Point(mp);
////                    virtualPoint = screenPoint;
////                    virtualPoint.x -= bounds.x;
////                    virtualPoint.y -= bounds.y;
////                    if (virtualPoint.x < 0) {
////                        virtualPoint.x *= -1;
////                    }
////                    if (virtualPoint.y < 0) {
////                        virtualPoint.y *= -1;
////                    }
//                    repaint();
//
//                }
//            });
//            timer.start();
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

        private void display(Point event) {
            screenPoint = event;
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
