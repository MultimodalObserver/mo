package core;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javafx.stage.FileChooser;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Celso
 */
public class NewProjectWizardPanel extends JPanel {
    WizardDialog wizard;
    JFileChooser fileChooser;
    private final JTextField locationField;
    private final JTextField nameField;
    private final JTextField folderField;
    
    public NewProjectWizardPanel(WizardDialog wizard) {
        this.wizard = wizard;
        super.setName("Name and Location");
        super.setLayout(new GridBagLayout());
        
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        GridBagConstraints c = new GridBagConstraints();
        
        JLabel nameLabel = new JLabel("Project Name:");
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
        
        JLabel locationLabel = new JLabel("Project Location:");
        locationField = new JTextField();
        locationField.setText(System.getProperty("user.dir"));
        locationField.getDocument().addDocumentListener(new DocumentListener() {
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
        
        JLabel folderLabel = new JLabel("Project Folder:");
        folderField = new JTextField(locationField.getText());
        folderField.setEditable(false);
        
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectLocation(e);
            }
        });
        
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(10, 5, 2, 5);
        c.anchor = GridBagConstraints.LINE_START;
        super.add(nameLabel, c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(10, 5, 2, 5);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        super.add(nameField, c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.insets = new Insets(2, 5, 2, 5);
        c.fill = GridBagConstraints.NONE;
        super.add(locationLabel, c);
        
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        super.add(locationField, c);
        
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        super.add(browseBtn, c);
        
        c.gridx = 0;
        c.gridy = 2;
        super.add(folderLabel, c);
        
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        super.add(folderField, c);
        
        c.gridy = 3;
        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        c.ipadx = 450;
        c.ipady = 150;
        super.add(new JLabel(""), c);
        
        wizard.setWarningMessage("A project name must be specified.");
    }
    
    
    private void selectLocation(ActionEvent e) {
        int returnVal = fileChooser.showOpenDialog(fileChooser);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            locationField.setText(file.getPath());
            //This is where a real application would open the file.
            //System.out.println("Opening: " + file.getName() + ".");
            updateState();
        } else {
            //System.out.println("Open command cancelled by user.");
        }
    }
    
    private void updateState(){
        File folder = new File(locationField.getText());
        File newFolder = new File(locationField.getText()+"/"+nameField.getText());
        folderField.setText(newFolder.getPath());
        if (nameField.getText().isEmpty()){
            wizard.setWarningMessage("A project name must be specified.");
            wizard.nullResult();
            wizard.disableFinish();
        } else if (locationField.getText().isEmpty()) {
            wizard.setWarningMessage("A project location must be specified.");
            wizard.nullResult();
            wizard.disableFinish();
        } else if (!folder.isDirectory()) {
            wizard.setWarningMessage("Project location doesn't exists.");
            wizard.nullResult();
            wizard.disableFinish();
        } else if (newFolder.exists()){
            wizard.disableFinish();
            wizard.setWarningMessage("Directory already exists.");
            wizard.nullResult();
        } else {
            wizard.addResult("projectFolder", newFolder.getPath());
            wizard.setWarningMessage("");
            wizard.enableFinish();
        }
    }
}
