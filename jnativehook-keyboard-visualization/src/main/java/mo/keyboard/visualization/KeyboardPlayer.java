package mo.keyboard.visualization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
import mo.visualization.Playable;
import org.apache.commons.io.input.ReversedLinesFileReader;

public class KeyboardPlayer implements Playable {

    private long start;
    private long end;
    private boolean stopped = false;
    
    private KeyboardEvent current;
    private KeyboardEvent nextEvent;

    private DisplayPanel pane;
    private RandomAccessFile file;
    
    private static final Logger logger = Logger.getLogger(KeyboardPlayer.class.getName());
    
    public KeyboardPlayer(File f) {
        try {
            file = new RandomAccessFile(f, "r");
            readLastTime(f);
            current = readNextEventFromFile();
            if (current != null) {
                start = current.time;
                nextEvent = readNextEventFromFile();
            }
            
            pane = new DisplayPanel();
            SwingUtilities.invokeLater(() -> {
                try {
                    DockableElement e = new DockableElement();
                    e.add(pane.getPanel());
                    DockablesRegistry.getInstance().addAppWideDockable(e);
                } catch (Exception ex) {
                    logger.log(Level.INFO, null, ex);
                }
            });

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

    private KeyboardEvent readNextEventFromFile() {
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
        switch (parts.length) {
            case 7:
                hasModifiers = false;
                break;
            case 8:
                hasModifiers = true;
                break;
            default:
                return null;
        }

        KeyboardEvent ev = new KeyboardEvent();
        ev.time = Long.parseLong(parts[0]);
        ev.type = KeyboardEventType.getEventTypeFromString(parts[1]);
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

        ev.keyLocation = KeyLocation.getKeyLocationFromString(parts[partsIndex].split("=")[1]);
        partsIndex++;

        ev.rawCode = Integer.parseInt(parts[partsIndex].split("=")[1]);

        if (ev.type.equals(KeyboardEventType.NATIVE_KEY_TYPED)
                && ev.keyCode == 0 && ev.rawCode == 13) {
            ev.keyChar = '\n';
        }

        return ev;
    }

    @Override
    public void pause() {
    }

    @Override
    public void seek(long desiredMillis) {
        if (desiredMillis < start
                || desiredMillis > end
                || desiredMillis == current.time
                || (desiredMillis > current.time &&
                    desiredMillis < nextEvent.time)) {
            return;
        }       
        
        KeyboardEvent event = current;

        if (desiredMillis < current.time) {
            try {
                file.seek(0);
                event = readNextEventFromFile();

            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        long marker;
        try {
            marker = file.getFilePointer();

            KeyboardEvent next = readNextEventFromFile();
            if (next == null) {
                return;
            }

            while (!(next.time > desiredMillis)) {
                event = next;

                marker = file.getFilePointer();
                next = readNextEventFromFile();
                
                if (next == null) { // no more events (end of file)
                    return;
                }
            }

            file.seek(marker);
            current = event;
            nextEvent = next;

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void display(KeyboardEvent ev) {
        if (stopped) {
            pane.clear();
            stopped = false;
        }
        pane.display(ev);
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
    public void play(long millis) {
        if ( (millis >= start) && (millis <= end)) {
            seek(millis);
            if (current.time == millis) {
                while (current.time == millis) {
                    display(current);
                    current = nextEvent;
                    nextEvent = readNextEventFromFile();
                }
            }
        }
    }

    @Override
    public void stop() {
        stopped = true;
        pause();
    }
}