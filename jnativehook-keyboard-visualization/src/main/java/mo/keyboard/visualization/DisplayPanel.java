package mo.keyboard.visualization;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DisplayPanel {

    private final JTextArea text;
    private final JPanel internal;

    public DisplayPanel() {
        //super()
        text = new JTextArea();
        text.setEditable(false);

        text.setText("");

        JScrollPane scroll = new JScrollPane(text);

        //setLayout(new BorderLayout());
        //add(scroll, BorderLayout.CENTER);
        internal = new JPanel();
        internal.setLayout(new BorderLayout());
        internal.add(scroll, BorderLayout.CENTER);
        internal.add(scroll);

    }

    public JPanel getPanel() {
        return internal;
    }

    public void display(KeyboardEvent event) {

        if (event.type.equals(KeyboardEventType.NATIVE_KEY_TYPED) /*|| 
                    event.type.equals(KeyboardEventType.NATIVE_KEY_PRESSED)*/) {

            if (event.rawCode == 8) {
                text.setText(text.getText().substring(0, text.getText().length() - 1));
            } else {
                //String str = String.valueOf(Character.forDigit(event.keyCode, 0));
                //logger.log(Level.INFO, Character.toString((char) event.keyChar));
                text.append(Character.toString(event.keyChar));
                //text.setText(text.getText()+str);
            }

//                SwingUtilities.invokeLater(() -> {
//                    
//                });
        }
    }

    public void clear() {
        text.setText("");
    }
}
