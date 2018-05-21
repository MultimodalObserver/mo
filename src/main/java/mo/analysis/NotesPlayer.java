package mo.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import mo.visualization.Playable;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;

public class NotesPlayer implements Playable {
    private long start;
    private long end;
    private final List<NotesRecorder> notesRecorders;
    private final List<Track> tracks;
    private final AnalysisTimePanel panel;

    private static final Logger logger = Logger.getLogger(NotesPlayer.class.getName());

    public NotesPlayer(List<NotesRecorder> notesRecorders) {
        this.notesRecorders = notesRecorders;
        tracks = new ArrayList<>();

        Track track;
        for (NotesRecorder nr : notesRecorders) {
            track = new Track(getTrackName(nr), getNotes(nr));
            tracks.add(track);
        }

        panel = new AnalysisTimePanel(this);

        SwingUtilities.invokeLater(() -> {
            try {
                DockableElement e = new DockableElement();
                e.add(panel);
                DockablesRegistry.getInstance().addAppWideDockable(e);
            } catch (Exception ex) {
                logger.log(Level.INFO, null, ex);
            }
        });
    }

    public List<Track> getTracks() {
        return tracks;
    }

    private TreeSet<Note> getNotes(NotesRecorder recorder) {
        TreeSet<Note> set = new TreeSet<>();
        BufferedReader reader = null;

        try {
            File file = recorder.getFile();
            reader = new BufferedReader(new FileReader(file));

            String line;
            String[] tokens;
            long beginTime;
            long endTime;
            String text;
            Note note;
            while ((line = reader.readLine()) != null) {
                tokens = line.split(",");

                try {
                    beginTime = Long.parseLong(tokens[0]);
                    endTime = Long.parseLong(tokens[1]);
                    text = tokens[2];
                    note = new Note(beginTime,endTime,text);
                    set.add(note);
                } catch (NumberFormatException ex) {
                    logger.log(Level.INFO, "line in the file does not conform to the format of NotesRecorder");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return set;
    }

    public String getTrackName(NotesRecorder notesRecorder) {
        String trackNameWithExtension = notesRecorder.getFile().getName();
        String trackName = trackNameWithExtension.substring(0, trackNameWithExtension.length()-(4+1));

        return trackName;
    }

    public void addNote(int trackIndex, Note note) {
        tracks.get(trackIndex).getNotes().add(note);
        saveNote(trackIndex,note);
    }

    public void saveNote(int trackIndex, Note note) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                notesRecorders.get(trackIndex).writeNote(note);
            }
        });

        thread.start();
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
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
    public void play(long ms) {
        if (ms < start) {
            play(start);
        } else if (ms > end) {
            play(end);
        } else {
            seek(ms);
        }
    }

	@Override
    public void stop() {

    }

    @Override
    public void seek(long ms) {
        panel.setTime(ms);
    }

    @Override
    public void pause() {

    }

    @Override
    public void sync(boolean sync){
        
    }
}