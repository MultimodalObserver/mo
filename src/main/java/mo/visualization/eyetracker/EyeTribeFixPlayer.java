package mo.visualization.eyetracker;

import com.google.gson.Gson;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.response.Response;
import com.theeyetribe.clientsdk.response.TrackerGetResponse;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.SwingUtilities;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
import mo.visualization.Playable;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.time.FastDateFormat;

public class EyeTribeFixPlayer implements Playable {

    private double speed = 1;
    private long start, end = -1;
    private boolean isPlaying = false;

    private RandomAccessFile file;

    TrackerGetResponse current;
    private static final Gson gson = new Gson();

    private static final Logger logger
            = Logger.getLogger(EyeTribeFixPlayer.class.getName());

    private final static FastDateFormat dateFormat
            = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");

    int linesCount = 0;

    FixationPanel panel;
    private long timeToSleep;
    private Thread thread;

    public EyeTribeFixPlayer(File file) {
        try {

            ReversedLinesFileReader rev = new ReversedLinesFileReader(file, Charset.defaultCharset());
            String lastLine = null;
            do {
                lastLine = rev.readLine();
            } while (lastLine == null && lastLine.trim().isEmpty());
            rev.close();
            TrackerGetResponse trackerRes = gson.fromJson(lastLine, TrackerGetResponse.class);
            if (trackerRes.category.equals("tracker")) {
                end = dateStringToMillis(trackerRes.values.frame.timeStampString);
            }

            this.file = new RandomAccessFile(file, "r");
            String line;

            line = this.file.readLine();
            if (line != null) {
                current = gson.fromJson(line, TrackerGetResponse.class);
                start = dateStringToMillis(current.values.frame.timeStampString);
            }

//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
                    panel = new FixationPanel(1920, 1080);
                    DockableElement d = new DockableElement();
                    d.add(panel);
                    DockablesRegistry.getInstance().addDockableInProjectGroup("", d);
//                }
//            });

        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private TrackerGetResponse readNextFromFile() {
        try {
            TrackerGetResponse res = null;
            do {
                
                String line = file.readLine();

                if (line != null) {
                    res = gson.fromJson(line, TrackerGetResponse.class);
                }
                
            } while ( !res.category.equals("tracker") );
            
            return res;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
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
                + g.smoothedCoordinates + " " + g.leftEye + " " + g.rightEye;
        return result;
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
    public void seek(long requestedMillis) {
        if (requestedMillis < start) {
            seek(start);
            timeToSleep = start - requestedMillis;
            if (isPlaying) {
                play();
            }
            return;
        }

        if (requestedMillis > end) {
            seek(end);
            isPlaying = false;
            return;
        }

        boolean playing = isPlaying;

        if (isPlaying) {
            isPlaying = false;
        }

        TrackerGetResponse res = current;

        if (requestedMillis < dateStringToMillis(current.values.frame.timeStampString)) {
            try {
                file.seek(0);
                res = getNext();

            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        long marker;
        try {
            marker = file.getFilePointer();

            TrackerGetResponse next = getNext();
            if (next == null) {
                return;
            }

            while (!(dateStringToMillis(next.values.frame.timeStampString) > requestedMillis)) {
                res = next;

                marker = file.getFilePointer();
                next = getNext();
            }

            file.seek(marker);
            current = res;
            timeToSleep = requestedMillis
                    - dateStringToMillis(current.values.frame.timeStampString);

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
        System.out.println("asd");
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

                TrackerGetResponse next = null;
                while (isPlaying) {
                    
                    if (current == null) {
                        current = getNext();
                    }
                    next = getNext();

                    if (current == null || next == null) {
                        System.out.println("no more KB events");
                        isPlaying = false;
                        interrupt();
                        return;
                    }

                    double x = current.values.frame.smoothedCoordinates.x;
                    double y = current.values.frame.smoothedCoordinates.y;

                    if (current.values.frame.state != GazeData.STATE_TRACKING_FAIL
                            && current.values.frame.state != GazeData.STATE_TRACKING_LOST
                            && !(x == 0 && y == 0)) {

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                panel.addGazeData(current.values.frame);
                            }
                        });

                    }

                    long sleep = (long) ((dateStringToMillis(next.values.frame.timeStampString)
                            - dateStringToMillis(current.values.frame.timeStampString)) / speed);
                    current = next;
                    if (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

        };
        thread.start();
    }

    TrackerGetResponse getNext() {
        TrackerGetResponse r = null;
        try {
            do {
                String line = file.readLine();
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
        EyeTribeFixPlayer p = new EyeTribeFixPlayer(f);

        JFrame fr = new JFrame();

        fr.add(p.panel);
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

        p.play();
    }
}
