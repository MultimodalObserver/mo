package mo.analysis;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.KeyEvent;

import java.awt.event.KeyListener;

public class TextBox extends JPanel  implements KeyListener {
	final private JTextArea textArea;
	final private JScrollPane textAreaScrollPane;

	public TextBox() {
        

        // JTextArea
        textArea = new JTextArea(2,1);
        textArea.setText("alonso");

        // JScrollPane
        textAreaScrollPane = new JScrollPane(textArea);
        textAreaScrollPane.setPreferredSize(new Dimension(150,30));
        textAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        textArea.getDocument().putProperty("filterNewlines", Boolean.TRUE);
        textArea.addKeyListener(this);

        // this
        this.setBounds(0,0,150,30);
		this.setOpaque(false);
        this.setVisible(false);
		add(textAreaScrollPane);
		
	}

	public void showme() {
		setVisible(true);
		requestFocus();
	}

	public String getText() {
		return textArea.getText();
	}

	@Override
	public void	keyPressed(KeyEvent e) {
		System.out.println("keyPressed");
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			System.out.println("EnterKey correcto correcto");
		}
	}

	@Override
	public void keyReleased(KeyEvent e){System.out.println("keyReleased");}

	@Override
	public void	keyTyped(KeyEvent e){System.out.println("keyTyped");}
}