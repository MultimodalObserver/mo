package mo.analysis;

import java.util.TreeSet;

public class Track {
    private String name;
    private TreeSet<Note> notes;

    public Track() {}

    public Track(String trackName, TreeSet<Note> trackNotes) {
        this.name = trackName;
        this.notes = trackNotes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeSet<Note> getNotes() {
        return notes;
    }

    public void setNotes(TreeSet<Note> notes) {
        this.notes = notes;
    }
}
