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

// #todo cargar los tracks de acuerdo a los plugins

// #todo guardar las notas

// #todo cargar las notas desde los archivos archivo










// #todo corregir la altura de la linea roja vertical
// #todo darle transparencia a la seleccion de mouse
// #todo panel con los nombres de los tracks junto al scrollpane
// #todo colores random para los tracks

// #todo corregir la altura de la seleccion de mouse
// #todo truncar los comentarios para que quepan en la selección

// #todo mover la scrollbar según la posición de la linea roja

// #todo demarcar con corchetes la zona de seleccion

// #todo editar notas

// #todo eliminar notas

// #todo pintar solo lo que se ve. actualmente se pinta todo el track, desde principio a fin


// #todo quitar los imports innecesarios, no importar paquetes, solo clases
// #todo quitar los println innecesarios
// #todo limpiar los comentarios que sobran

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

    // transparencia
    private AlphaComposite alpha04 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
    private AlphaComposite alpha07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
    private AlphaComposite opaque = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);

    private TextBox textBox;

    // capas 
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
        // layeredPane.setBackground(Color.black);
        
        // layeredPane.addMouseMotionListener(this);
        // layeredPane.addMouseListener(this);

        // todo AnalysisTimePanel(VisualizationPlayer player, configs)

        setBackground(Color.black);
        this.player = player;

		drawingPane = new DrawingPane();

        // drawingPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        // drawingPane.setBackground(Color.white);
        // drawingPane.setPreferredSize(new Dimension(2000,2000));
        // drawingPane.setOpaque(true);

        // MouseListener
        addMouseListener(this);
        drawingPane.addMouseListener(this);

        // MouseMotionListener
        addMouseMotionListener(this);
        drawingPane.addMouseMotionListener(this);

        // ActionListener
        newComment = new JMenuItem("Nuevo comentario");
        newComment.setActionCommand("newComment");
        newComment.addActionListener(this);

        // inicio nuevo

        // add(scroller); //en lugar de agregar el scroller al AnalysisTimePanel, lo agrego a layeredPane, y luego agrego el layeredPane al AnalysisTimePanel
        topPanel = new JPanel();
        topPanel.setBackground(Color.RED);
        topPanel.setBounds(100,100,25,25);
        topPanel.setOpaque(true);

        anotherPanel = new JPanel();
        anotherPanel.setBackground(Color.blue);
        anotherPanel.setBounds(100,100,50,50);
        anotherPanel.setOpaque(true);

        // cajita de texto
        textBox = new TextBox();

        layeredPane.add(drawingPane, new Integer(0));
        layeredPane.add(textBox, new Integer(1));
        // layeredPane.setOpaque(false);
        // layeredPane.add(topPanel, new Integer(0));
        // layeredPane.add(anotherPanel, new Integer(1));
        
        // add(topPanel);

        // scroller = new JScrollPane(drawingPane);
        scroller = new JScrollPane(layeredPane);
        scroller.setPreferredSize(new Dimension(100,100));


        add(scroller);
        // drawingPane.update();

        // fin nuevo

        startTime = player.getStart();
        System.out.println("startTime = " + startTime);
        endTime = player.getEnd();
        miliseconds = (int) endTime - (int) startTime;
        time = 0;

        pluginTracks = new ArrayList<>();
        
        // se crean los tracks a partir de las configuraciones
        PluginTrack track;
        // todo for(PluginTrack track : configs)
        
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
            System.out.println("i = " + i);
            System.out.println("id = " + conf.getId());
            i++;
        }


        // aNote.setStartTime(500);
        // aNote.setEndTime(800);

        // aNote = new Note("hola", 500,800);

        // pluginTracks.get(0).addNote(aNote);

        // aNote.setStartTime(1000);
        // aNote.setEndTime(1300);

        aNote = new Note("mundo", 1000,1300);

        pluginTracks.get(0).addNote(aNote);

        trackWidth = (int) Math.ceil((double) miliseconds / (double) 100) * (int) msPerPixel;

        // textBox = new TextBox();
        // textBox.setVisible(false);
        // drawingPane.add(textBox);

        // layeredPane.update();
        // drawingPane.repaint(); // no funca

    }

    @Override
	protected void paintComponent(Graphics g) {
        System.out.println("paintComponent del AnalysisTimePanel");
	    super.paintComponent(g);

	    Dimension panelDimension = getSize();
	    width = (int) panelDimension.getWidth();
	    height = (int) panelDimension.getHeight();
	    scroller.setPreferredSize(panelDimension);
        drawingPane.revalidate();
	}

    public void setTime(long time) {
        this.time = (int) (time - startTime);
        System.out.println("ms: " + this.time);
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

    // paint the mouse selection
    public void paintMouseSelection(Graphics2D g) {
        /*
        this.xPos = pxMousePressed;
        this.width = Math.abs(pxMousePressed - pxMouseDragged);
        int height = 50; // todo corregir esta altura. es el offset más la altura de los tracks

        if(pxMousePressed > pxMouseDragged) {
            this.xPos = pxMouseDragged;
        }

        g.setColor(Color.green);
        // g.setComposite(alpha04);
        beginSelectTime = xPos;
        g.fillRect(this.xPos,0,this.width,height);
        */

        // inicio nuevo

        int height = offset+numeroPlugins*trackHeight; // todo corregir esta altura. es el offset más la altura de los tracks
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
        // fin nuevo
        g.setComposite(opaque);
    }

    // pintar los plugin tracks
    public void paintPluginTracks(Graphics2D g2d) {
        int i=0;
        for(PluginTrack track : pluginTracks) {
            // track.paint(g2d,0,offset+(i*trackHeight),trackWidth, trackHeight);
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
            // g2d.drawString(truncateComment(note.getComment()), pxAtMs(note.getStartTime()), track.y2-8);
        }

    }

    // #todo
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
                // #todo meterlo en una funcion
                Note anotherNote = new Note(textBox.getText(),msAtPx(beginSelectPos.x),msAtPx(endSelectPos.x)); 
                int indexTrack = getIndexTrack(rightClick.y);
                pluginTracks.get(indexTrack).addNote(anotherNote);
                textBox.setVisible(false);

                repaint();
            }

            leftClick = e.getPoint();
        }
        System.out.println("mouseClicked = " + e.getPoint());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        System.out.println("mouseEntered = " + e.getPoint());
    }

    @Override
    public void mouseExited(MouseEvent e) {
        System.out.println("mouseExited = " + e.getPoint());
    }

    @Override
    public void mousePressed(MouseEvent e) {

        pressed = e.getPoint();

        pxMousePressed = (int) e.getPoint().getX();

        if (SwingUtilities.isLeftMouseButton(e)) {
            // if(leftClick == null) {
            //     beginSelectPos = e.getPoint();
            //     System.out.println("primer e.getPoint().x = " + e.getPoint().x);
            //     System.out.println("primer e.getPoint().y = " + e.getPoint().y);
            // }

            // if(leftClick != null) {
            //     if (e.getPoint().x != leftClick.x && e.getPoint().y != leftClick.y) {
            //         System.out.println("e.getPoint().x = " + e.getPoint().x);
            //         System.out.println("leftClick.x = " + leftClick.x);
            //         System.out.println("e.getPoint().y = " + e.getPoint().y);
            //         System.out.println("leftClick.y = " + leftClick.y);
            //         beginSelectPos = e.getPoint();
            //     }
            // }

            // inicio nuevo
            beginSelectPos = pressed;
            // fin nuevo

        }

        // si es click derecho
        if(SwingUtilities.isRightMouseButton(e)) {
            rightClick = e.getPoint();
            if(beginSelectPos != null && endSelectPos != null) {
                if(rightClick.x >= beginSelectPos.x && rightClick.x <= endSelectPos.x) {
                    if(getIndexTrack((int) rightClick.y) != -1) {

                        // inicio accion
                        if(popupMenu == null) {
                            createMenu();
                        }

                        popupMenu.show(this,e.getX(),e.getY());
                        // fin accion
                    }
                }
            }
        }

        System.out.println("mousePressed = " + e.getPoint());
        System.out.println("time  = " + msAtPx((int) e.getPoint().getX()));
        System.out.println("track = " + getIndexTrack((int) e.getPoint().getY()));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pxMouseReleased = (int) e.getPoint().getX();

        if (SwingUtilities.isLeftMouseButton(e)) {
            released = e.getPoint();
            if (released.x == pressed.x && released.y == pressed.y) {
                System.out.println("son iguales");
                System.out.println(previousPressed);
                System.out.println(pressed);
                System.out.println(released);

                pressed = previousPressed;
                beginSelectPos = pressed;
            } else {
                System.out.println("son distintos");
                System.out.println(previousPressed);
                System.out.println(pressed);
                System.out.println(released);

                previousPressed = pressed;
            }
            
            
        }

        
        System.out.println("mouseReleased = " + e.getPoint());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        
                 
        /*
        pxMouseDragged = (int) e.getPoint().getX();

        System.out.println("mouseDragged = " + e.getPoint());

        if(pxMouseDragged != pxMousePressed) {
            repaint();
        }
        */

        // inicio nuevo
        if (SwingUtilities.isLeftMouseButton(e)) {
            endSelectPos = e.getPoint();
            if (beginSelectPos.x != endSelectPos.x) {
                repaint();
            }
        }
        // fin nuevo
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("actionPerformed");
        if(e.getActionCommand().equals("newComment")) {
            // JPanel unPanel = new JPanel();
            // JTextArea textArea = new JTextArea(5, 30);
            // JScrollPane scrollPane = new JScrollPane(textArea);


            //
            // drawingPane.add(scrollPane, BorderLayout.CENTER);
            
            // textBox = new TextBox();
            textBox.setVisible(true);
            textBox.setLocation(rightClick.x,rightClick.y);
            textBox.requestFocus();
            // add(textBox);
            // repaint();
            //textBox.showme();
        }
    }

    public class DrawingPane extends JPanel { // addMouseListener(this);

    	public DrawingPane() {
            setFont(new Font("Arial Unicode MS", Font.PLAIN, 11));
			bufferedImage = new BufferedImage(100,100, BufferedImage.TYPE_INT_ARGB);
			g2d = bufferedImage.createGraphics();

            this.setOpaque(false);
            this.setBounds(0,0,2000,2000);
            // this.setPreferredSize(new Dimension(2000,2000));
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

            // paint vertical line 
            g2d.setColor(Color.red);
            g2d.drawLine(pxAtMs((int) time),0,pxAtMs((int) time),offset+numeroPlugins*trackHeight);

            // pinta los tracks
            paintPluginTracks(g2d);
            // pluginTracks.get(0).paint(g2d);

            // paint selection
            if(pxMousePressed != pxMouseDragged) {
                paintMouseSelection(g2d);
            }
            // g2d.setComposite(opaco);

        }
    }

}