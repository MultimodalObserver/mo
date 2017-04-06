package mo.mouse.visualization;

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
import javax.swing.SwingUtilities;
import mo.visualization.Playable;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class MousePlayer implements Playable {

    private long start, end, currentTime;

    private RandomAccessFile file;

    private PlayerPane pane;

    private MouseEvent currentEvent, nextEvent;

    private static final Logger logger = Logger.getLogger(MousePlayer.class.getName());

    public MousePlayer(File file) {
        try {
            this.file = new RandomAccessFile(file, "r");

            String line = this.file.readLine();
            List<Rectangle> bounds = parseScreens(line);

            String lastLine;
            try (ReversedLinesFileReader rev = new ReversedLinesFileReader(file, Charset.defaultCharset())) {
                lastLine = null;
                do {
                    lastLine = rev.readLine();
                    if (lastLine == null) {
                        break;
                    }
                } while (lastLine.trim().isEmpty());
            }

            MouseEvent lastEvent = parseEventFromLine(lastLine);
            if (lastEvent != null) {
                end = lastEvent.time;
            }

            currentEvent = readNextEventFromFile();
            if (currentEvent != null) {
                start = currentEvent.time;
                nextEvent = readNextEventFromFile();
            }

            
            pane = new PlayerPane(bounds);
            
            SwingUtilities.invokeLater(() -> {
                
                pane.display(currentEvent);
//                DockableElement e = new DockableElement();
//                e.add(pane);
//                DockablesRegistry.getInstance().addAppWideDockable(e);
//                
//                System.out.println(start + " " + end + " " + currentEvent);
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
    public void pause() {
    }
    
    public long getCurrentTime() {
        return currentTime;
    }
    

    @Override
    public void seek(long desiredMillis) {
        if (desiredMillis < start) {
            seek(start);
            return;
        }

        if (desiredMillis > end) {
            seek(end);
            return;
        }
        
        currentTime = desiredMillis;
        
        if (desiredMillis == currentEvent.time) {
            pane.display(currentEvent);
            return;
        }

        if (desiredMillis > currentEvent.time && desiredMillis < nextEvent.time) {
            return;
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
            nextEvent = next;
            
            pane.display(currentEvent);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void play(long millis) {
        if (millis < start) {
            play(start);
        } else if (millis > end) {
            play(end);
        } else {
            seek(millis);
        }
    }
    
    //TODO

//    @Override
//    public void play() {
//        isPlaying = true;
//
//        thread = new Thread() {
//            @Override
//            public void run() {
//                if (timeToSleep > 0) {
//                    try {
//                        sleep(timeToSleep);
//                    } catch (InterruptedException ex) {
//                        logger.log(Level.SEVERE, null, ex);
//                    }
//                }
//                timeToSleep = 0;
//
//                MouseEvent nextEvent = null;
//                while (isPlaying) {
//                    nextEvent = readNextEventFromFile();
//                    if (currentEvent == null || nextEvent == null) {
//                        isPlaying = false;
//                        interrupt();
//
//                        System.out.println("No more data in file");
//                        return;
//                    }
//                    
//                    SwingUtilities.invokeLater(new Runnable(){
//                        @Override
//                        public void run() {
//                            pane.display(currentEvent);
//                        }
//                    });
//                    
//                    long sleepTime = (long) ((nextEvent.time - currentEvent.time) / speed);
//                    currentEvent = nextEvent;
//                    if (sleepTime > 0) {
//                        try {
//                            sleep(sleepTime);
//                        } catch (InterruptedException ex) {
//                            logger.log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            }
//
//        };
//        thread.start();
//    }

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
}
