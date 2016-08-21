package mo.organization;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.util.xml.XElement;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import mo.core.ui.GridBConstraints;
import static mo.core.ui.Utils.centerOnScreen;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.StorableDockable;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class OrganizationDockable extends DockableElement implements StorableDockable {

    public OrganizationDockable(String id, CAction... actions) {
        super(id, actions);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project " + id);

        DefaultMutableTreeNode participants = new DefaultMutableTreeNode("Participants");

        root.add(participants);

        JMenuItem addParticipantMenu = new JMenuItem("add participant");
        addParticipantMenu.addActionListener((ActionEvent e) -> {
            ParticipantDialog dialog = new ParticipantDialog();
            Participant participant = dialog.showDialog();
            if (participant != null) {
                System.out.println("yess");
            } else {
                System.out.println("nouu");
            }
        });

        JPopupMenu participantsMenu = new JPopupMenu();
        participantsMenu.add(addParticipantMenu);

        JTree tree = new JTree(root);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    if (event.isPopupTrigger()) {
                        int row = tree.getRowForLocation(event.getX(), event.getY());
                        if (row == -1) {
                            return;
                        }
                        tree.setSelectionRow(row);
                        DefaultMutableTreeNode selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        System.out.println("selected " + selected);
                        if (selected.equals(participants)) {
                            participantsMenu.show((JComponent) event.getSource(), event.getX(), event.getY());
                        }
                    }
                }
            }

        });

        JScrollPane scroll = new JScrollPane(tree);
        add(scroll);
    }

    private Properties toProperties(ResourceBundle resource) {
        Properties result = new Properties();
        Enumeration<String> keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            result.put(key, resource.getString(key));
        }
        return result;
    }

    @Override
    public String toFileContent() {
        return "orgaDock";
    }

    @Override
    public DockableElement dockableFromFile(String fileContent) {
        return new OrganizationDockable("asd");
    }

    private class Participant {

        String id;
        String name;
        Date date;
        String notes;
    }

    private class ParticipantDialog extends JDialog {
        
        Participant participant;
        
        JTextField idField;
        JTextField nameField;
        JLabel errorLabel;
        JButton accept;

        public ParticipantDialog() {
            super(null, "New participant", Dialog.ModalityType.APPLICATION_MODAL);

            participant = new Participant();
            
            setLayout(new GridBagLayout());
            GridBConstraints g = new GridBConstraints();
            g.a(GridBConstraints.FIRST_LINE_START)
                    .f(GridBConstraints.BOTH)
                    .i(new Insets(5, 5, 5, 5));

            add(new JLabel("Id"), g.gx(0).gy(0));
            add(new JLabel("Name"), g.gy(1));
            add(new JLabel("Date"), g.gy(2));
            add(new JLabel("Notes"), g.gy(3));

            idField = new JTextField();
            idField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateState();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateState();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateState();
                }
            });
            nameField = new JTextField();
            nameField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateState();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateState();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateState();
                }
            });

            JTextArea notesArea = new JTextArea();
            notesArea.setRows(7);
            JScrollPane scroll = new JScrollPane(notesArea);

            Properties p = toProperties(ResourceBundle.getBundle("org.jdatepicker.i18n.Text", Locale.getDefault()));

            UtilDateModel model = new UtilDateModel();
            model.setValue(new Date());
            JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
            JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateComponentFormatter());

            add(idField, g.gx(1).gy(0).wx(1.0));
            add(nameField, g.gy(1));
            add(datePicker, g.gy(2));
            add(scroll, g.gx(0).gy(4).gw(2));
            
            errorLabel = new JLabel("An ID and name must be specified");
            errorLabel.setForeground(Color.red);
            add(errorLabel, g.gx(0).gy(5).gw(2));

            JPanel buttonsPanel = new JPanel(new GridBagLayout());

            accept = new JButton("Accept");
            accept.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ( !idField.getText().isEmpty() && !nameField.getText().isEmpty()) {
                        participant.id = idField.getText();
                        participant.name = nameField.getText();
                        participant.date = (Date) datePanel.getModel().getValue();
                        participant.notes = notesArea.getText();
                        setVisible(false);
                        dispose();
                    } else {
                    
                    }
                }
            });
            accept.setEnabled(false);
            
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    participant = null;
                    setVisible(false);
                    dispose();
                }
            });
            
            g.clear().i(new Insets(5, 5, 15, 5));
            buttonsPanel.add(accept, g.gx(0).gy(0));
            buttonsPanel.add(cancel, g.gx(1).gy(0).i(new Insets(5, 5, 15, 15)));

            g.clear().gx(0).gy(6).a(GridBConstraints.LAST_LINE_END).wx(1).wy(1).gw(2);
            add(buttonsPanel, g);

            setPreferredSize(new Dimension(300, 400));
            pack();
            centerOnScreen(this);
            
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    participant = null;
                    super.windowClosing(e);
                }
                
            });
        }

        public Participant showDialog() {
            setVisible(true);
            return participant;
        }
        
        private void updateState() {
            if (idField.getText().isEmpty() || nameField.getText().isEmpty()) {
                errorLabel.setText("An ID and name must be specified");
                accept.setEnabled(false);
            } else {
                errorLabel.setText("");
                accept.setEnabled(true);
            }
        }
    }

    public static void main(String[] args) {

        OrganizationDockable o = new OrganizationDockable("test");

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CControl c = new CControl(f);
        f.setContentPane(c.getContentArea());
        c.addDockable(o);
        o.setVisible(true);
        f.pack();
        centerOnScreen(f);

        f.pack();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                f.setVisible(true);
            }
        });
    }
}
