package mo.core;

import mo.pruebas.ClassContainer;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

/**
 *
 * @author Celso
 */
public class PluginSelectionDialog extends JDialog {

    JList pluginsList;
    ListSelectionModel listSelection;
    HashSet<ClassContainer> plugins;
    ClassContainer selectedPlugin;

    public PluginSelectionDialog(JFrame frame, HashSet<ClassContainer> plugins) {
        super(frame, "Plugin Selection", JDialog.ModalityType.APPLICATION_MODAL);
        //setLocationRelativeTo(null);
        super.setLocation(400, 150);
        super.setSize(400, 500);

        this.plugins = plugins;

        DefaultListModel dlm = new DefaultListModel();
        plugins.stream().forEach((plugin) -> {
            dlm.addElement(plugin.getName());
        });

        JLabel m = new JLabel("Select a plugin");
        m.setAlignmentX(LEFT_ALIGNMENT);


        pluginsList = new JList();
        pluginsList.setModel(dlm);
        listSelection = pluginsList.getSelectionModel();
        

        JScrollPane listScroller = new JScrollPane(pluginsList);

        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        JTextArea desc = new JTextArea();
        desc.setRows(3);

        desc.setEditable(false);

        JScrollPane descScroller = new JScrollPane(desc);

        descScroller.setAlignmentX(LEFT_ALIGNMENT);

        JLabel l = new JLabel("Description");

        JPanel listPanel = new JPanel();

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        listPanel.add(m);
        listPanel.add(listScroller);
        listPanel.add(Box.createVerticalGlue());
        listPanel.add(l);
        listPanel.add(descScroller);

        JButton okB = new JButton("Ok");
        okB.setEnabled(false);
        okB.addActionListener((ActionEvent e) -> {
            setVisible(false);
            dispose();
        });
        
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener((ActionEvent e) -> {
            selectedPlugin = null;
            setVisible(false);
            dispose();
        });

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(cancel);
        buttons.add(Box.createRigidArea(new Dimension(10, 0)));
        buttons.add(okB);
        
        Container contentPane = super.getContentPane();
        contentPane.add(listPanel, BorderLayout.CENTER);
        contentPane.add(buttons, BorderLayout.PAGE_END);
        
        listSelection.addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()){
                int index = pluginsList.getSelectedIndex();
                if (index >= 0 && index < plugins.size()){
                    selectedPlugin = (ClassContainer) (plugins.toArray())[index];
                    desc.setText(selectedPlugin.getDescription());
                    okB.setEnabled(true);
                } else {
                    selectedPlugin = null;
                    okB.setEnabled(false);
                }
            }
        });

        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                selectedPlugin = null;
            }
            @Override
            public void windowClosing(WindowEvent e) {
                selectedPlugin = null;
            }
        });

    }

    public ClassContainer showDialog() {
        setVisible(true);
        return selectedPlugin;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        PluginSelectionDialog d = new PluginSelectionDialog(f, null);
        d.setVisible(true);
    }
}
