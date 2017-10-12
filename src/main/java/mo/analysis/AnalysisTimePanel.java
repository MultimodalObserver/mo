package mo.analysis;

import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.BorderLayout;
import mo.analysis.TimeRuler;
import mo.visualization.VisualizationPlayer;
import javax.swing.JScrollPane;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.AlphaComposite;
import java.awt.Point;
import java.awt.event.MouseMotionListener;
import java.awt.Font;
import mo.visualization.VisualizableConfiguration;

public class AnalysisTimePanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {
	private BufferedImage bufferedImage;
	private Graphics2D g2d;
	private int height;
	private Dimension panelDimension;
    private JScrollBar horizontalScrollBar;
    private DrawingPane drawingPane;
	private JScrollPane scroller;
    private TimeRuler ruler;
    private List<PluginTrack> pluginTracks;
    private VisualizationPlayer player;
    private float msPerPixel = 10;
    private long startTime;
    private long endTime;
    private int time;
    private int offset = 40;
    private int trackHeight = 25;
    private int miliseconds;
    private int trackWidth;
    private JMenuItem newComment;
    private JPopupMenu popupMenu;
    private int beginSelectTime;
    private int endSelectTime;
    private int beginSelectX;
    private int endSelectX;

    private int pxMousePressed;
    private int pxMouseReleased;
    private int pxMouseDragged;
    
    private Point rightClick;

    private AlphaComposite alpha04 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
    private AlphaComposite alpha07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    private AlphaComposite opaque = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

    private TextBox textBox;

    private JLayeredPane layeredPane;
    private JPanel topPanel;
    private JPanel anotherPanel;

    private int xPos;
    private int width;

    private Point endSelectPos;
    private Point beginSelectPos;

    private Point leftClick;
    private Point previousPressed = null;
    private Point pressed = null;
    private Point released = null;
    private int numeroPlugins;

	public AnalysisTimePanel(VisualizationPlayer player) {

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(3000,3000));
        
        setBackground(Color.black);
        this.player = player;

		drawingPane = new DrawingPane();

        addMouseListener(this);
        drawingPane.addMouseListener(this);

        addMouseMotionListener(this);
        drawingPane.addMouseMotionListener(this);

        newComment = new JMenuItem("Nuevo comentario");
        newComment.setActionCommand("newComment");
        newComment.addActionListener(this);

        topPanel = new JPanel();
        topPanel.setBackground(Color.RED);
        topPanel.setBounds(100,100,25,25);
        topPanel.setOpaque(true);

        anotherPanel = new JPanel();
        anotherPanel.setBackground(Color.blue);
        anotherPanel.setBounds(100,100,50,50);
        anotherPanel.setOpaque(true);

        textBox = new TextBox();

        layeredPane.add(drawingPane, new Integer(0));
        layeredPane.add(textBox, new Integer(1));
        
        scroller = new JScrollPane(layeredPane);
        scroller.setPreferredSize(new Dimension(100,100));
        add(scroller);

        startTime = player.getStart();
        endTime = player.getEnd();
        miliseconds = (int) endTime - (int) startTime;
        time = 0;

        pluginTracks = new ArrayList<>();
        
        PluginTrack track;
        
        Note aNote = new Note("hola", 150, 300);

        int y = offset;
        int y1,y2;
        numeroPlugins = 3;
        for(int i=0; i<numeroPlugins; i++) {
            y1 = y;
            y2 = y + trackHeight;
            track = new PluginTrack(player.getStart(), player.getEnd(),y1,y2);
            track.addNote(aNote);
            pluginTracks.add(track);
            y = y2;
        }

        int i=0;
        for (VisualizableConfiguration conf : player.getConfigs()) {
            i++;
        }

        aNote = new Note("mundo", 1000,1300);

        pluginTracks.get(0).addNote(aNote);

        trackWidth = (int) Math.ceil((double) miliseconds / (double) 100) * (int) msPerPixel;
    }

    @Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);

	    Dimension panelDimension = getSize();
	    width = (int) panelDimension.getWidth();
	    height = (int) panelDimension.getHeight();
	    scroller.setPreferredSize(panelDimension);
        drawingPane.revalidate();
	}

    public void setTime(long time) {
        this.time = (int) (time - startTime);
        repaint();
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
        int height = offset+numeroPlugins*trackHeight;
        if(beginSelectPos != null && endSelectPos != null) {
            width = Math.abs(beginSelectPos.x - endSelectPos.x);
            
            if(beginSelectPos.x < endSelectPos.x) {
                xPos = beginSelectPos.x;
            } else {
                xPos = endSelectPos.x;
            }

            g.setColor(Color.green);
            g.setComposite(alpha04);
            g.fillRect(xPos,0,width,height);
        }
        g.setComposite(opaque);
    }

    public void paintPluginTracks(Graphics2D g2d) {
        int i=0;
        for(PluginTrack track : pluginTracks) {
            g2d.setColor(Color.blue);
            g2d.setComposite(alpha04);
            g2d.fillRect(0,offset+(i*trackHeight),trackWidth,trackHeight);
            i++;
        }

        paintNotes(g2d);
    }

    public void paintNotes(Graphics2D g2d) {
        for (PluginTrack track : pluginTracks ) {
            paintTrackNotes(g2d, track);
        }
    }

    public void paintTrackNotes(Graphics2D g2d, PluginTrack track) {
        g2d.setColor(Color.black);
        g2d.setComposite(opaque);

        for (Note note : track.getNotes()) {
            g2d.drawString(note.getComment(), pxAtMs(note.getStartTime()), track.y2-8);
        }

    }

    public String truncateComment(String comment, int width) {
        return comment;
    }

    private void createMenu() {
        popupMenu = new JPopupMenu("Popup Menu");
        popupMenu.add(newComment);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {

            if (textBox.isVisible()) {
                Note anotherNote = new Note(textBox.getText(),msAtPx(beginSelectPos.x),msAtPx(endSelectPos.x)); 
                int indexTrack = getIndexTrack(rightClick.y);
                pluginTracks.get(indexTrack).addNote(anotherNote);
                textBox.setVisible(false);

                repaint();
            }

            leftClick = e.getPoint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

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
                        popupMenu.show(this,e.getX(),e.getY());
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pxMouseReleased = (int) e.getPoint().getX();

        if (SwingUtilities.isLeftMouseButton(e)) {
            released = e.getPoint();
            if (released.x == pressed.x && released.y == pressed.y) {
                pressed = previousPressed;
                beginSelectPos = pressed;
            } else {
                previousPressed = pressed;
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
            textBox.setVisible(true);
            textBox.setLocation(rightClick.x,rightClick.y);
            textBox.requestFocus();
        }
    }

    public class DrawingPane extends JPanel {

    	public DrawingPane() {
            setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
			bufferedImage = new BufferedImage(100,100, BufferedImage.TYPE_INT_ARGB);
			g2d = bufferedImage.createGraphics();

            this.setOpaque(false);
            this.setBounds(0,0,2000,2000);
            this.setBackground(Color.white);
    	}

        public void update() {
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            ruler = new TimeRuler(player.getStart(), player.getEnd());
            ruler.paint(g2d,msPerPixel);

            g2d.setColor(Color.red);
            g2d.drawLine(pxAtMs((int) time),0,pxAtMs((int) time),offset+numeroPlugins*trackHeight);

            paintPluginTracks(g2d);

            if(pxMousePressed != pxMouseDragged) {
                paintMouseSelection(g2d);
            }
        }
    }
}