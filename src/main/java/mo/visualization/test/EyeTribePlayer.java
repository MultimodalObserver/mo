package mo.visualization.test;

import com.google.gson.Gson;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.response.Response;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import mo.visualization.Playable;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class EyeTribePlayer implements Playable {

    private double speed = 1000;
    private long currentTime, start, end = -1;
    private boolean isPlaying = false;

    private RandomAccessFile raf;

    private TestPane pane;
    LiveHeatMap hmap;
    
    TrackerGetResponse current;
    Gson gson = new Gson();
    
    private static final Logger logger = Logger.getLogger(EyeTribePlayer.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    int linesCount = 0;

    public EyeTribePlayer(File file) {
        try {
            Gson g = new Gson();
            
            
            ReversedLinesFileReader rev = new ReversedLinesFileReader(file, Charset.defaultCharset());
            String lastLine = null;
            do {
                lastLine = rev.readLine();
            } while ( lastLine == null && lastLine.trim().isEmpty() );
            rev.close();
            TrackerGetResponse trackerRes = g.fromJson(lastLine, TrackerGetResponse.class);
            if (trackerRes.category.equals("tracker")) {
                end = dateStringToMillis(trackerRes.values.frame.timeStampString);
            }
            
            
            raf = new RandomAccessFile(file, "r");
            String line;

            pane = new TestPane();
            hmap = new LiveHeatMap(1080, 1920);

            line = raf.readLine();
            if (line != null) {
                current = g.fromJson(line, TrackerGetResponse.class);
                start = dateStringToMillis(current.values.frame.timeStampString);
            }
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    private static long dateStringToMillis(String dateStr) {
        Date date;
        try {
            date = dateFormat.parse(dateStr);
            return date.getTime();
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    
    String responseToString(Response r) {
        String result = r.request + " " + r.statuscode + " " + r.category;
        if (r.category.equals("tracker")) {
            return result + " " + tgrToStr((TrackerGetResponse) r);
        }
        return result;
    }
    
    String tgrToStr(TrackerGetResponse r) {
        return GazeDataToString(r.values.frame);
    }
    
    String GazeDataToString(GazeData g) {
        String result = "";
        result += g.timeStamp + " " + g.timeStampString + " " + g.state + " "
                + g.stateToString() + " " + g.isFixated + " " + g.rawCoordinates + " "
                + g.smoothedCoordinates + " " +g.leftEye + " " + g.rightEye;
        return result;
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
//
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

        isPlaying = playing;
    }

//    @Override
//    public void play() {
//        isPlaying = true;
//        TrackerGetResponse next = null;
//        while (isPlaying) {
//            if (current == null) {
//                current = getNext();
//            }
//            next = getNext();
//            
////            pane.display(new Point(
////                    (int) current.values.frame.smoothedCoordinates.x,
////                    (int) current.values.frame.smoothedCoordinates.y));
//
//            double x = current.values.frame.smoothedCoordinates.x;
//            double y = current.values.frame.smoothedCoordinates.y;
//
//            if (current.values.frame.state != GazeData.STATE_TRACKING_FAIL &&
//                    current.values.frame.state != GazeData.STATE_TRACKING_LOST
//                    && !(x == 0 && y==0) ) {
//                hmap.update((int) x, (int) y);
//            }
//            
//            if (linesCount % 1000 == 0) {
//                System.out.println(linesCount);
//                if (linesCount > 118000) {
//                    isPlaying = false;
//                    System.out.println("end");
//                    System.exit(0);
//                }
//            }
//            
//            long sleep = (long) ((dateStringToMillis(next.values.frame.timeStampString) - dateStringToMillis(current.values.frame.timeStampString)) / speed);
//            current = next;
//            try {
//                Thread.sleep(sleep);
//            } catch (InterruptedException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }
//
//        }
//    }
    
    TrackerGetResponse getNext() {
        TrackerGetResponse r = null;
        try {
            do {
                String line = raf.readLine();
                linesCount++;
                if (line != null) {
                    r = gson.fromJson(line, TrackerGetResponse.class);
                }
            } while (r == null || !r.category.equals("tracker"));
            return r;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        return end;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println();
        File f = new File("C:\\Users\\Celso\\Desktop\\06.txt");
        EyeTribePlayer p = new EyeTribePlayer(f);

        JFrame fr = new JFrame();
        

        fr.add(p.hmap);
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

        p.play(0); //TODO

    }

    @Override
    public void play(long millis) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stop() {
    }
    @Override
    public void sync(boolean sync) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class TestPane extends JPanel {

        private double scale;
        private final List<Rectangle> screenBounds;

        private Point virtualPoint;
        private Point screenPoint;

        private Rectangle virtualBounds = new Rectangle(0, 0, 0, 0);

        public TestPane() {
            ArrayList<Rectangle> screens = new ArrayList<>();
            screens.add(new Rectangle(1920, 1080));
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

                g2d.drawString(screenPoint.x+","+screenPoint.y, x, y);
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

        private void display(Point p) {
            screenPoint = p;
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
