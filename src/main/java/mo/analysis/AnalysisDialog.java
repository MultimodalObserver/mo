package mo.analysis;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import static mo.core.DataFileFinder.findFilesCreatedBy;
import mo.core.ui.GridBConstraints;
import mo.core.ui.WizardDialog;
import mo.organization.Configuration;
import mo.visualization.VisualizableConfiguration;

public class AnalysisDialog {

    WizardDialog dialog;

    List<Configuration> configurations;

    ArrayList<JCheckBox> checkBoxs;
    ArrayList<JComboBox> filesComboBoxes;

    List<File> files;

    File projectRoot;

    JPanel filesPane;

    GridBConstraints gbc;

    public AnalysisDialog(List<Configuration> configs, File project) {
        gbc = new GridBConstraints();
        projectRoot = project;
        dialog = new WizardDialog(null, "Visualization setup");
        JPanel configsPanel = new JPanel();
        configsPanel.setName("Select configurations");

        configsPanel.setLayout(new GridBagLayout());
        GridBConstraints g = new GridBConstraints();

        configurations = new ArrayList<>();

        configurations = new ArrayList<>();
        checkBoxs = new ArrayList<>();
        filesComboBoxes = new ArrayList<>();
        for (Configuration configuration : configs) {
            JCheckBox c = new JCheckBox(configuration.getId());
            checkBoxs.add(c);
            configsPanel.add(c, g);
            c.putClientProperty("configuration", configuration);
            c.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    updateState();
                }
            });
        }

        dialog.addPanel(configsPanel);

        filesPane = new JPanel(new GridBagLayout());
        filesPane.setName("Select files");

        dialog.addPanel(filesPane);

        // filesPane = new JPanel(new GridBagLayout());
        // filesPane.setName("select analysis configs");

        // dialog.addPanel(filesPane);
        
        dialog.addActionListener(new WizardDialog.WizardListener() {
            @Override
            public void onStepChanged() {
                stepChanged();
            }
        });
        
        dialog.setWarningMessage("");
    }

    private void updateState() {
        if (dialog.getCurrentStep() == 0) {
            Configuration aConfiguration;
            for (JCheckBox checkBox : checkBoxs) {
                // for (Class inter : checkBox.getClientProperty("configuration").getClass().getInterfaces()){
                //     System.out.println("la clase = " + inter.getSimpleName());
                // }
                Configuration rc;
                System.out.println("la clase = " + (checkBox.getClientProperty("configuration").getClass().getInterfaces()[0]).getSimpleName());
                if("VisualizableConfiguration".equals((checkBox.getClientProperty("configuration").getClass().getInterfaces()[0]).getSimpleName())) {

                    rc = (VisualizableConfiguration) checkBox.getClientProperty("configuration");
                } else {

                rc = (AnalyzableConfiguration) checkBox.getClientProperty("configuration");
                }
                if (checkBox.isSelected()) {
                    if (!configurations.contains(rc)) {
                        configurations.add(rc);
                    }
                } else if (configurations.contains(rc)) {
                    configurations.remove(rc);
                }
            }

            if (configurations.size() > 0) {
                dialog.enableNext();
            } else {
                dialog.disableNext();
            }
        } else if (dialog.getCurrentStep() == 1) {
            int count = 0;
            for (JComboBox filesComboBox : filesComboBoxes) {
                Object o =  filesComboBox.getSelectedItem();
                if ( ! (o instanceof String) ) {
                    count++;
                }
            }
            if (count == filesComboBoxes.size()) {
                dialog.enableFinish();
            } else {
                dialog.disableFinish();
            }
        }
    }

    private void stepChanged() {
        if (dialog.getCurrentStep() == 0) {
            dialog.disableBack();
            filesPane.removeAll();// = new JPanel();
            filesComboBoxes.clear();
            dialog.disableFinish();
            dialog.disableBack();
        } else if (dialog.getCurrentStep() == 1) {
            dialog.disableNext();
            dialog.enableBack();

            int row = 0;
            for (Configuration configuration : configurations) {
                if (configuration instanceof VisualizableConfiguration) {
                    filesPane.add(new JLabel(configuration.getId()), gbc.gy(row++));
                    VisualizableConfiguration c = (VisualizableConfiguration) configuration;
                    List<String> creators = c.getCompatibleCreators();
                    files = findFilesCreatedBy(projectRoot, creators);
                    JComboBox b = new JComboBox();
                    b.putClientProperty("configuration", configuration);
                    b.addItem("Select a file");
                    for (File file : files) {
                        
                        try {
                            b.addItem(new FilePath(projectRoot, file));
                            FilePath fPath = new FilePath(projectRoot, file);
                            String archivoCombobox = fPath.toString();
                        } catch (IOException ex) {
                            Logger.getLogger(AnalysisDialog.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    filesComboBoxes.add(b);
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            updateState();
                        }
                    });
                    filesPane.add(b, gbc.gy(row++));
                    
                }
            }
            
        } 
        // else if(dialog.getCurrentStep() == 2) {
        //     dialog.enableBack();
        // }
        updateState();
    }

    public static void main(String[] args) {
//        MouseVisConfiguration c = new MouseVisConfiguration();
//        c.setId("nombre");
//        c.getCompatibleCreators();
//        List<Configuration> cs = new ArrayList<>();
//        cs.add(c);
//        VisualizationDialog2 d = new VisualizationDialog2(cs, new File("C:\\Users\\Celso\\Desktop\\ejemplo"));
//        d.dialog.showWizard();
    }
    
    public boolean show() {
        HashMap res = dialog.showWizard();
        if (res == null) {
            return false;
        } else {
            return true;
        }
    }
    
    public HashMap<Configuration, File> getConfigurationsAndFiles() {
        HashMap result = new HashMap<>();
        for (JComboBox filesComboBox : filesComboBoxes) {
            FilePath f = (FilePath) filesComboBox.getSelectedItem();
            
            result.put(filesComboBox.getClientProperty("configuration"), f.file);
        }
        return result;
    }
    
    public List<VisualizableConfiguration> getConfigurations() {
        ArrayList<VisualizableConfiguration> list = new ArrayList<>();
        for (JComboBox filesComboBox : filesComboBoxes) {
            FilePath f = (FilePath) filesComboBox.getSelectedItem();
            VisualizableConfiguration c = (VisualizableConfiguration) filesComboBox.getClientProperty("configuration");
            c.addFile(f.file);
            list.add(c);
        }
        return list;
    }
    
    class FilePath {

        File from;
        File file;

        public FilePath(File from, File to) throws IOException {
            this.file = new File(to.getCanonicalPath());
            this.from = from;
        }

        @Override
        public String toString() {
            Path p = file.toPath();
            Path r = from.toPath();
            return r.relativize(p).toString();
        }

    }
}
