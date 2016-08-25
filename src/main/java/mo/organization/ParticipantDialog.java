package mo.organization;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mo.core.ui.GridBConstraints;
import static mo.core.ui.Utils.centerOnScreen;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

public class ParticipantDialog extends JDialog {

    private Participant participant;

    JTextField idField;
    JTextField nameField;
    JLabel errorLabel;
    JButton accept;
    List<Participant> orgParticipants;

    public ParticipantDialog(OrganizationDockable organization) {
        super(null, "New participant", Dialog.ModalityType.APPLICATION_MODAL);
        orgParticipants = organization.getParticipants();
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
        accept.addActionListener((ActionEvent e) -> {
            if (!idField.getText().isEmpty() && !nameField.getText().isEmpty()) {
                participant.id = idField.getText();
                participant.name = nameField.getText();
                participant.date = (Date) datePanel.getModel().getValue();
                participant.notes = notesArea.getText();
                setVisible(false);
                dispose();
            } else {
                
            }
        });
        accept.setEnabled(false);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener((ActionEvent e) -> {
            participant = null;
            setVisible(false);
            dispose();
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

    public Properties toProperties(ResourceBundle resource) {
        Properties result = new Properties();
        Enumeration<String> keys = resource.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            result.put(key, resource.getString(key));
        }
        return result;
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
            if (isParticipantIdUnique(idField.getText())){
                errorLabel.setText("");
                accept.setEnabled(true);
            } else {
                errorLabel.setText("ID must be unique");
                accept.setEnabled(false);
            }
        }
    }
    
    private boolean isParticipantIdUnique(String id) {
        for (Participant p : orgParticipants) {
            if (p.id.equals(id))
                return false;
        }
        return true;
    }
}
