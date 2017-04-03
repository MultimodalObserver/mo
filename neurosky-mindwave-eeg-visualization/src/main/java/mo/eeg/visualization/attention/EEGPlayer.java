package mo.eeg.visualization.attention;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
import mo.visualization.Playable;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class EEGPlayer implements Playable {

    private double speed = 1;
    private long timeToSleep, start, end;
    private boolean isPlaying = false;

    private RandomAccessFile file;

    private EEGData current;
    LiveWave wave;
    
    int count = 0;
    
    private Thread thread;

    private static final Logger logger = Logger.getLogger(EEGPlayer.class.getName());

    public EEGPlayer(File file) {
        try {
            readLastTime(file);
            this.file = new RandomAccessFile(file, "r");
            current = next();
            start = current.time;
            
            SwingUtilities.invokeLater(new Runnable() {
            @Override
                public void run() {
                    wave = new LiveWave();
                    wave.addVariable("Att", 0, 100, Color.blue);
                    DockableElement d = new DockableElement();
                    d.add(wave);
                    DockablesRegistry.getInstance().addDockableInProjectGroup("", d);                
                }
            });
            
//            wave = new LiveWave();
//            wave.addVariable("Att", 0, 100, Color.blue);


        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    EEGData next() {
        try {
            EEGData data = new EEGData();

            String line = file.readLine();          
            while (line.contains("Eyeblink")) { //ignore eyeblink records
                line = file.readLine();
            }

            int ind = line.lastIndexOf("#");
            data.time = parseTimestamp(line.substring(0, ind));

            data.poorSignal = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.delta = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.theta = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.alpha1 = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.alpha2 = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.beta1 = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.beta2 = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.gamma1 = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.gamma2 = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.attention = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());

            line = file.readLine();
            data.meditation = Integer.parseInt(line.substring(line.lastIndexOf(":") + 1).trim());
            
            count++;
            if (count % 1000 == 0) {
                System.out.println(count);
            }

            return data;
        } catch (Exception ex) {
            System.out.println(current);
            pause();
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
        //return null;
    }

    //11:43:03:968#Time:1465314183,96879
    static long parseTimestamp(String str) {
        String[] parts = str.split("#");

        if (parts.length == 2) {

            String secondsStr = parts[1].substring(
                    parts[1].lastIndexOf(":") + 1,
                    parts[1].lastIndexOf(","));
            long seconds = Long.parseLong(secondsStr) * 1000;

            String millisStr = parts[0].substring(parts[0].lastIndexOf(":") + 1);
            long millis = Long.parseLong(millisStr);

            return seconds + millis;
        }

        return -1;
    }

    void readLastTime(File file) {
        try {
            ReversedLinesFileReader rev = new ReversedLinesFileReader(file, Charset.defaultCharset());
            String lastLine = null;
            do {
                lastLine = rev.readLine();
            } while (lastLine == null && lastLine.trim().isEmpty());
            rev.close();

            end = parseTimestamp(lastLine.substring(0, lastLine.lastIndexOf("#")));
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

        fr.add(p.wave);
        fr.setPreferredSize(new Dimension(400, 400));
        fr.setSize(400, 400);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
    public void pause() {
        isPlaying = false;
    }

    @Override
    public void seek(long requestedMillis) {
        if (requestedMillis < start) {
            seek(start);
            timeToSleep = start - requestedMillis;
            if (isPlaying) {
                play(0); //TODO
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

        EEGData res = current;

        if (requestedMillis < res.time) {
            try {
                file.seek(0);
                res = next();

            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        long marker;
        try {
            marker = file.getFilePointer();

            EEGData next = next();
            if (next == null) {
                return;
            }

            while (!(next.time > requestedMillis)) {
                res = next;

                marker = file.getFilePointer();
                next = next();
            }

            file.seek(marker);
            current = res;
            timeToSleep = requestedMillis - current.time;

            isPlaying = playing;
            if (isPlaying) {
                play(0); //TODO
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void play(long millis) {
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
                
                EEGData next = null;
                while (isPlaying) {

                    if (current == null) {
                        current = next();
                    }

                    next = next();
                    
                    if (current == null || next == null) {
                        System.out.println("no more KB events");
                        isPlaying = false;
                        interrupt();
                        return;
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                wave.addData("Att", current.time, current.attention);
                            }
                        });

                    long sleep = (long) ((next.time - current.time) / speed);

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

    class EEGData {

        long time;
        int poorSignal, delta, theta, alpha1, alpha2,
                beta1, beta2, gamma1, gamma2, attention, meditation;

        @Override
        public String toString() {
            return time + " sig:" + poorSignal + " d:" + delta + " t:" + theta
                    + " a1:" + alpha1 + " a2:" + alpha2 + " b1:" + beta1 + " b2:" + beta2
                    + " g1:" + gamma1 + " g2:" + gamma2
                    + " att:" + attention + " med:" + meditation;
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
}
