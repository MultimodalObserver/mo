package mo.analysis;

import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Dimension;
import mo.visualization.VisualizationPlayer;
import javax.swing.JScrollPane;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.AlphaComposite;
import java.awt.Point;
import java.awt.event.MouseMotionListener;
import java.awt.Font;
import java.util.TreeSet;
import java.util.Iterator;
import javax.swing.JScrollBar;
import mo.analysis.TextBox.NewNoteListener;

public class AnalysisTimePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener, NewNoteListener {
    private BufferedImage bufferedImage;
    private Graphics2D g2d;
    private TimeRuler ruler;
    private VisualizationPlayer player;
    private JPopupMenu popupMenu;
    private Point endSelectPos;
    private Point beginSelectPos;
    private Point leftClick;
    private Point rightClick;
    private Point previousPressed = null;
    private Point pressed = null;
    private Point released = null;
    private long startTime;
    private long endTime;
    private int time;
    private int miliseconds;
    private int trackWidth;
    private int height;
    private int pxMousePressed;
    private int pxMouseDragged;
    private int xPos;
    private int width;
    private final int offset = 40;
    private final int trackHeight = 25;
    private final int margin = 20;
    private final List<PluginTrack> pluginTracks = new ArrayList<>();
    private final DrawingPane drawingPane;
    private final JScrollPane notesTrackScroller;
    private final JScrollPane namesTrackScroller;
    private final float msPerPixel = 10;
    private final int trackNameWidth = 300;
    private final JMenuItem newComment;
    private final AlphaComposite alpha04 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
    private final AlphaComposite alpha07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    private final AlphaComposite opaque = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
    private final TextBox textBox;
    private final JLayeredPane notesLayeredPane;
    private final TrackNamePanel trackNamePanel;
    private final JLayeredPane namesLayeredPanel;
    private final JPanel panelContenedor;
    private final NotesPlayer notesPlayer;
    private final JScrollBar scrollBar;

    public AnalysisTimePanel(NotesPlayer notesPlayer) {
        this.notesPlayer = notesPlayer;
        panelContenedor = this;
        
        time = 0;
        startTime = notesPlayer.getStart();
        endTime = notesPlayer.getEnd();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBackground(Color.black);
        addMouseListener(this);
        addMouseMotionListener(this);

        trackNamePanel = new TrackNamePanel();
        namesLayeredPanel = new JLayeredPane();
        namesLayeredPanel.setPreferredSize(new Dimension(300,3000));
        namesLayeredPanel.add(trackNamePanel, new Integer(0));
        namesTrackScroller = new JScrollPane(namesLayeredPanel);
        namesTrackScroller.setPreferredSize(new Dimension(300,300));
        namesTrackScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        namesTrackScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(namesTrackScroller);

        drawingPane = new DrawingPane();
        drawingPane.addMouseListener(this);
        drawingPane.addMouseMotionListener(this);
        textBox = new TextBox();
        textBox.addNewNoteListener(this);
        notesLayeredPane = new JLayeredPane();
        notesLayeredPane.setPreferredSize(new Dimension(3000,3000));
        notesLayeredPane.add(drawingPane, new Integer(0));
        notesLayeredPane.add(textBox, new Integer(1));
        notesTrackScroller = new JScrollPane(notesLayeredPane);
        notesTrackScroller.setPreferredSize(new Dimension(100,100));
        add(notesTrackScroller);
        scrollBar = notesTrackScroller.getHorizontalScrollBar();

        newComment = new JMenuItem("Nueva nota");
        newComment.setActionCommand("newComment");
        newComment.addActionListener(this);
        
        PluginTrack track;
        int y = offset;
        int y1,y2;
        Note note;
        Iterator<Note> it;
        String trackName;
        TreeSet<Note> set;
        int colorIndex=0;
        for (Track aTrack : notesPlayer.getTracks()) {
            trackName = aTrack.getName();
            set = aTrack.getNotes();
            it = set.iterator();
            y1 = y;
            y2 = y + trackHeight;
            track = new PluginTrack(y1,y2);
            track.setName(trackName);
            track.setColor(getColor(colorIndex));
            pluginTracks.add(track);
            while(it.hasNext()) {
                note = it.next();
                track.addNote(note);
            }
            y=y2;
            colorIndex++;
        }
    }

    public Color getColor(int indexColor) {
        if (indexColor % 2 == 0) {
            return Color.BLUE;
        }

        return Color.CYAN;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension panelDimension = getSize();
        width = (int) panelDimension.getWidth();
        this.height = (int) panelDimension.getHeight();
        notesTrackScroller.setPreferredSize(panelDimension);
        drawingPane.revalidate();
    }

    public long getAbsolutTime(int relativeTime) {
        return (long) relativeTime + startTime;
    }

    public long getRelativeTime(long absolutTime) {
        return (int) (absolutTime - startTime);
    }

    public void setTime(long time) {
        this.time = (int) (time - notesPlayer.getStart());
        updateScrollBarPosition();
        repaint();
    }

    public void updateScrollBarPosition() {
        int cursorPosition = pxAtMs(time);
        int scrollBarPosition = scrollBar.getValue();

        if (cursorPosition < scrollBarPosition ||
                cursorPosition  > scrollBarPosition + panelContenedor.getSize().width-namesTrackScroller.getSize().width-margin) {
            if(cursorPosition > 50) {
                scrollBar.setValue(cursorPosition-margin);
            }
        }
    }

    public int pxAtMs(int ms) {
        return ms/(int) msPerPixel;
    }

    public int msAtPx(int px) {
        return px * (int) msPerPixel;
    }

    public int getIndexTrack(int y) {
        if ((y < offset)) {
            return -1;
        }

        if(y > ((trackHeight * 3) + offset)) {
            return -1;
        }

        y = y - offset;

        return y/trackHeight;
    }

    public void paintMouseSelection(Graphics2D g) {
        int selectionHeight = offset+pluginTracks.size()*trackHeight;
        
        if(beginSelectPos != null && endSelectPos != null) {
            width = Math.abs(beginSelectPos.x - endSelectPos.x);
            
            if(beginSelectPos.x < endSelectPos.x) {
                xPos = beginSelectPos.x;
            } else {
                xPos = endSelectPos.x;
            }

            g.setColor(Color.green);
            g.setComposite(alpha04);
            g.fillRect(xPos,0,width,selectionHeight);
        }

        g.setComposite(opaque);
    }

    public void paintPluginTracks(Graphics2D g2d) {
        int i=0;
        if (miliseconds == 0) {
            miliseconds = (int) (endTime - startTime);
        }
        
        if (trackWidth == 0) {
            trackWidth = (int) Math.ceil((double) miliseconds / (double) 100) * (int) msPerPixel;
        }

        for(PluginTrack track : pluginTracks) {
            g2d.setColor(track.getColor());
            g2d.setComposite(alpha04);
            g2d.fillRect(0,offset+(i*trackHeight),trackWidth,trackHeight);
            i++;
        }

        writeNotes(g2d);
    }

    public void writeNotes(Graphics2D g2d) {
        for (PluginTrack track : pluginTracks ) {
            paintTrackNotes(g2d, track);
        }
    }

    public void paintTrackNotes(Graphics2D g2d, PluginTrack track) {
        g2d.setColor(Color.black);
        g2d.setComposite(opaque);

        for (Note note : track.getNotes()) {
            g2d.drawString(note.getComment(), pxAtMs((int) getRelativeTime(note.getStartTime())), track.y2-8);
        }

    }

    public void writeTrackNames(Graphics2D g2d) {
        g2d.setColor(Color.black);
        g2d.setComposite(opaque);

        for (PluginTrack track : pluginTracks) {
            g2d.drawString(track.getName(), 0, track.y2-8);
        }
    }

    public void paintTrackNames(Graphics2D g2d) {
        int i=0;
        for(PluginTrack track : pluginTracks) {
            g2d.setColor(track.getColor());
            g2d.setComposite(alpha04);
            g2d.fillRect(0,offset+(i*trackHeight),trackNameWidth,trackHeight);
            i++;
        }
    }

    public String truncateComment(String comment, int width) {
        return comment;
    }

    private void createMenu() {
        popupMenu = new JPopupMenu("Popup Menu");
        popupMenu.add(newComment);
    }

    public void loadTrack(int indexTrack) {
        TreeSet notesTree = notesPlayer.getTracks().get(indexTrack).getNotes();
        Iterator<Note> it = notesTree.iterator();
        Note note; 
        PluginTrack track = pluginTracks.get(indexTrack);
        track.deleteNotes();
        while(it.hasNext()) {
            note = it.next();
            track.addNote(note);
        }
        pluginTracks.set(indexTrack,track);
    }
    
    public void addNewNote() {
        Note newNote = new Note(getAbsolutTime(msAtPx(beginSelectPos.x)), getAbsolutTime(msAtPx(endSelectPos.x)),textBox.getText());
        int indexTrack = getIndexTrack(rightClick.y);
        notesPlayer.addNote(indexTrack,newNote);
        loadTrack(indexTrack);
        textBox.hideme();

        repaint();
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (textBox.isVisible()) {
                addNewNote();
            }
            leftClick = e.getPoint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        pressed = e.getPoint();

        pxMousePressed = (int) e.getPoint().getX();

        if (SwingUtilities.isLeftMouseButton(e)) {
            beginSelectPos = pressed;
        }

        if(SwingUtilities.isRightMouseButton(e)) {
            rightClick = e.getPoint();
            if(beginSelectPos != null && endSelectPos != null) {
                if(rightClick.x >= beginSelectPos.x && rightClick.x <= endSelectPos.x) {
                    if(getIndexTrack((int) rightClick.y) != -1) {
                        if(popupMenu == null) {
                            createMenu();
                        }
                        popupMenu.show(notesLayeredPane,e.getX(),e.getY());
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            released = e.getPoint();
            if (released.x == pressed.x && released.y == pressed.y) {
                pressed = previousPressed;
                beginSelectPos = pressed;
            } else {
                previousPressed = pressed;
                if (beginSelectPos.x > endSelectPos.x) {
                    int inicio = beginSelectPos.x;
                    int fin = endSelectPos.x;
                    beginSelectPos.x = fin;
                    endSelectPos.x = inicio;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            endSelectPos = e.getPoint();
            if (beginSelectPos.x != endSelectPos.x) {
                repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("newComment")) {
            textBox.setLocation(rightClick.x,rightClick.y);
            textBox.setText("");
            textBox.showme();
        }
    }

    @Override
    public void newNote() {
        addNewNote();
    }
    
    public class TrackNamePanel extends JPanel {
        public TrackNamePanel() {
            setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
            setOpaque(false);
            setBounds(0,0,10000,2000);
            setBackground(Color.white);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            paintTrackNames(g2d);
            writeTrackNames(g2d);
        }
    }

    public class DrawingPane extends JPanel {

    	public DrawingPane() {
            setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
            bufferedImage = new BufferedImage(100,100, BufferedImage.TYPE_INT_ARGB);
            g2d = bufferedImage.createGraphics();

            this.setOpaque(false);
            this.setBounds(0,0,10000,2000);
            this.setBackground(Color.white);
    	}

        public void update() {
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (startTime == 0) {
                startTime = notesPlayer.getStart();
            }

            if (endTime == 0) {
                endTime = notesPlayer.getEnd();

                miliseconds = (int) (endTime - startTime);
                int layeredPaneWidth = ((int) Math.ceil((double) miliseconds / (double) 100) * (int) msPerPixel) + 50;
                int layeredPaneHeight = offset + pluginTracks.size() * trackHeight;

                notesLayeredPane.setPreferredSize(new Dimension(layeredPaneWidth,layeredPaneHeight));
            }

            Graphics2D g2d = (Graphics2D) g;
            ruler = new TimeRuler(notesPlayer.getStart(), notesPlayer.getEnd());
            ruler.paint(g2d,msPerPixel);

            g2d.setColor(Color.red);
            g2d.drawLine(pxAtMs((int) time),0,pxAtMs((int) time),offset+pluginTracks.size()*trackHeight);

            paintPluginTracks(g2d);

            if(pxMousePressed != pxMouseDragged) {
                paintMouseSelection(g2d);
            }
        }
    }
}
