package mo.analysis;

import mo.visualization.Playable;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.NumberFormatException;

import java.util.Iterator;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;



public class NotesPlayer implements Playable {

    private long start;
    private long end;
    private List<NotesRecorder> notesRecorders;
    private List<TreeSet> notesForTrack;

    private final AnalysisTimePanel panel;

    private static final Logger logger = Logger.getLogger(NotesPlayer.class.getName());

    public NotesPlayer(List<NotesRecorder> notesRecorders) {
        this.notesRecorders = notesRecorders;
        notesForTrack = new ArrayList<>();

        for (NotesRecorder nr : notesRecorders) { // por cada archivo de NoteRecorder un TreeSet
            notesForTrack.add(getNotes(nr));
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

    public List<TreeSet> getNotesForTrack() {
        return notesForTrack;
    }

    private TreeSet<Note> getNotes(NotesRecorder recorder) {
        System.out.println("getNotes");
        TreeSet<Note> set = new TreeSet<Note>();
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
                    System.out.println("la linea no cumple con el formato");
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

        Iterator<Note> it = set.iterator();
        Note anotherNote;
        while(it.hasNext()) {
            anotherNote = it.next();
            System.out.println("tree begin = " + anotherNote.getStartTime());
            System.out.println("tree end = " + anotherNote.getEndTime());
            System.out.println("tree texto = " + anotherNote.getComment());
        }
        return set;
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
        if (ms < start) { // como la reproduccion es respecto de los timestamps absolutos, es necesario hacer estas verificaciones
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
        System.out.println("seek(" + ms + ")");
        panel.setTime(ms);
    }

    @Override
    public void pause() {

    }
}