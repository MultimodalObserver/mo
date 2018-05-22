package mo.analysis;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class TextBox extends JPanel  implements KeyListener {
    final private JTextArea textArea;
    final private JScrollPane textAreaScrollPane;
    private List<NewNoteListener> listeners;

    public TextBox() {
        listeners = new ArrayList<>();
        textArea = new JTextArea(2,1);
        textArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        textArea.addKeyListener(this);

        textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setPreferredSize(new Dimension(150,30));
        textAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        add(textAreaScrollPane);
        setFocusable(true);
        setBounds(0,0,150,30);
        setOpaque(false);
        setVisible(false);
    }

    public void showme() {
        setVisible(true);
        requestFocusInWindow();
        textArea.setFocusable(true);
        textArea.requestFocus();
    }

    public void hideme() {
        setVisible(false);
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public String getText() {
        return textArea.getText();
    }

    public void nuevaNota() {
        System.out.println();
    }
    
    public void addNewNoteListener(NewNoteListener listener) {
        listeners.add(listener);
    }
    
    public void notifyListeners() {
        for (NewNoteListener listener : listeners) {
            listener.newNote();
        }
    }

    @Override
    public void	keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            notifyListeners();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void	keyTyped(KeyEvent e) {}

    interface NewNoteListener {
        void newNote();
    }
}