package mo.visualization.keyboard;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
import mo.visualization.Playable;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class KeyboardPlayer implements Playable {

    private double speed = 1;
    private RandomAccessFile file;

    private long start, end;
    private KeyboardEvent current;

    private DisplayPanel pane;

    private boolean isPlaying;

    private Thread thread;
    private long timeToSleep;

    private static final Logger logger = Logger.getLogger(KeyboardPlayer.class.getName());

    public KeyboardPlayer(File f) {
        try {
            file = new RandomAccessFile(f, "r");
            readLastTime(f);
            current = readNextFromFile();
            if (current != null) {
                start = current.time;
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    pane = new DisplayPanel();

                    DockableElement e = new DockableElement();
                    e.add(pane);
                    DockablesRegistry.getInstance().addAppWideDockable(e);
                }

            });
            
            System.out.println("start:"+start+" end:"+end);

        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void readLastTime(File f) {
        try (ReversedLinesFileReader rev = new ReversedLinesFileReader(f, Charset.defaultCharset())) {
            String lastLine = null;
            do {
                lastLine = rev.readLine();
                if (lastLine == null) {
                    return;
                }
            } while (lastLine.trim().isEmpty());
            KeyboardEvent e = parseEventFromLine(lastLine);
            end = e.time;
            rev.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private KeyboardEvent readNextFromFile() {
        try {
            String line = file.readLine();
            if (line != null) {
                KeyboardEvent ev = parseEventFromLine(line);
                if (ev != null) {
                    return ev;
                } else {
                    return null;
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private KeyboardEvent parseEventFromLine(String line) {
        String[] parts = line.split(",");

        boolean hasModifiers;
        if (parts.length == 7) {
            hasModifiers = false;
        } else if (parts.length == 8) {
            hasModifiers = true;
        } else {
            System.out.println("parse kb ev failed");
            return null;
        }

        KeyboardEvent ev = new KeyboardEvent();
        ev.time = Long.parseLong(parts[0]);

        switch (parts[1]) {
            case "NATIVE_KEY_PRESSED":
                ev.type = KeyboardEventType.NATIVE_KEY_PRESSED;
                break;

            case "NATIVE_KEY_TYPED":
                ev.type = KeyboardEventType.NATIVE_KEY_TYPED;
                break;

            case "NATIVE_KEY_RELEASED":
                ev.type = KeyboardEventType.NATIVE_KEY_RELEASED;
                break;

            case "NATIVE_KEY_FIRST":
                ev.type = KeyboardEventType.NATIVE_KEY_FIRST;
                break;

            case "NATIVE_KEY_LAST":
                ev.type = KeyboardEventType.NATIVE_KEY_LAST;
                break;

            default:
                break;
        }

        ev.keyCode = Integer.parseInt(parts[2].split("=")[1]);

        ev.keyText = parts[3].split("=")[1];

        if (parts[4].contains("'")) {
            ev.keyChar = parts[4].charAt(parts[4].length() - 2);
        }

        int partsIndex = 5;
        if (hasModifiers) {
            ev.modifiers = parts[5].split("=")[1];
            partsIndex++;
        }

        switch (parts[partsIndex].split("=")[1]) {

            case "KEY_LOCATION_STANDARD":
                ev.keyLocation = KeyLocation.KEY_LOCATION_STANDARD;
                break;

            case "KEY_LOCATION_NUMPAD":
                ev.keyLocation = KeyLocation.KEY_LOCATION_NUMPAD;
                break;

            case "KEY_LOCATION_LEFT":
                ev.keyLocation = KeyLocation.KEY_LOCATION_LEFT;
                break;

            case "KEY_LOCATION_RIGHT":
                ev.keyLocation = KeyLocation.KEY_LOCATION_RIGHT;
                break;

            default:
                ev.keyLocation = KeyLocation.KEY_LOCATION_UNKNOWN;
                break;
        }
        partsIndex++;

        ev.rawCode = Integer.parseInt(parts[partsIndex].split("=")[1]);

        if (ev.type.equals(KeyboardEventType.NATIVE_KEY_TYPED)
                && ev.keyCode == 0 && ev.rawCode == 13) {
            ev.keyChar = '\n';
        }

        return ev;
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
        System.out.println("KB seek:"+desiredMillis);
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

        KeyboardEvent event = current;

        if (desiredMillis < current.time) {
            try {
                file.seek(0);
                event = readNextFromFile();

            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        long marker;
        try {
            marker = file.getFilePointer();

            KeyboardEvent next = readNextFromFile();
            if (next == null) {
                return;
            }

            while (!(next.time > desiredMillis)) {
                event = next;

                marker = file.getFilePointer();
                next = readNextFromFile();
            }

            file.seek(marker);
            current = event;
            timeToSleep = desiredMillis - current.time;
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
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        return end;
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

                KeyboardEvent next = null;
                while (isPlaying) {
                    next = readNextFromFile();
                    if (current == null || next == null) {
                        System.out.println("no more KB events");
                        isPlaying = false;
                        interrupt();
                        return;
                    }
                    
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                             pane.display(current);
                        }
                    });

                    long sleepTime = (long) ((next.time - current.time) / speed);
                    current = next;
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

    private enum KeyboardEventType {
        NATIVE_KEY_PRESSED,
        NATIVE_KEY_TYPED,
        NATIVE_KEY_RELEASED,
        NATIVE_KEY_FIRST,
        NATIVE_KEY_LAST
    }

    private enum KeyLocation {
        KEY_LOCATION_LEFT,
        KEY_LOCATION_NUMPAD,
        KEY_LOCATION_RIGHT,
        KEY_LOCATION_STANDARD,
        KEY_LOCATION_UNKNOWN
    }

    private class KeyboardEvent {

        long time;
        KeyboardEventType type;
        int keyCode;
        String keyText;
        char keyChar;
        int rawCode;
        KeyLocation keyLocation;
        String modifiers;
    }

    private class DisplayPanel extends JPanel {

        public JTextArea text;

        public DisplayPanel() {
            //super()
            text = new JTextArea();
            text.setEditable(false);

            JScrollPane scroll = new JScrollPane(text);

            setLayout(new BorderLayout());
            add(scroll, BorderLayout.CENTER);

        }

        public void display(KeyboardEvent event) {
            if (event.type.equals(KeyboardEventType.NATIVE_KEY_TYPED)) {
                if (event.rawCode == 8) {
                    text.setText(text.getText().substring(0, text.getText().length() - 1));
                } else {
                    text.append(String.valueOf(event.keyChar));
                }
            }
        }
    }
}
