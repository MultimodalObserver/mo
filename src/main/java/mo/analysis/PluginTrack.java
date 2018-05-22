package mo.analysis;

import java.awt.Color;
import java.util.List;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class PluginTrack {
    private Color color;
    private List<Note> notes;
    private String name;
    public int y1;
    public int y2;

    public PluginTrack(int y1, int y2) {
        this.y1 = y1;
        this.y2 = y2;
        notes = new ArrayList<>();
    }

    public void deleteNotes() {
        this.notes.clear();
    }

    public void paint(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(color);
        g2d.fillRect(x,y,width,height);
    }

    public void addNote(Note note) {
        this.notes.add(note);
    }

    public void removeNote(int index) {
        this.notes.remove(index);
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public void editNote(int index, String comment) {
        notes.get(index).setComment(comment);
    }

    public List<Note> getNotes() {
        return notes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
