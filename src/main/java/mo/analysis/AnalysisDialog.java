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
import mo.core.ui.GridBConstraints;
import mo.core.ui.WizardDialog;
import mo.organization.Configuration;
import mo.visualization.VisualizableConfiguration;
import mo.organization.StagePlugin;
import static mo.core.DataFileFinder.findFilesCreatedBy;
import mo.core.I18n;


public class AnalysisDialog {
    ArrayList<JCheckBox> checkBoxs;
    ArrayList<JComboBox> filesComboBoxes;
    List<File> files;
    File projectRoot;
    JPanel filesPane;
    GridBConstraints gbc;
    WizardDialog dialog;
    List<Configuration> configurations;
    
    private StagePlugin notesPlugin;
    private PlayableAnalyzableConfiguration notesConfiguration = null;
    private I18n i18n;

    public AnalysisDialog(StagePlugin notesPlugin, List<Configuration> configs, File project) {
        i18n = new I18n(AnalysisDialog.class);
        this.notesPlugin = notesPlugin;

        gbc = new GridBConstraints();
        projectRoot = project;
        dialog = new WizardDialog(null, i18n.s("AnalysisStage.visualizationSetup"));
        JPanel configsPanel = new JPanel();
        configsPanel.setName(i18n.s("AnalysisStage.selectConfigs"));

        configsPanel.setLayout(new GridBagLayout());
        GridBConstraints g = new GridBConstraints();

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
        filesPane.setName(i18n.s("AnalysisStage.selectFiles")); // marcador

        dialog.addPanel(filesPane);

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
                Configuration rc;
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

            updateNotePlugin();

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

    private void updateNotePlugin() {
        for (Configuration noteConfig : notesPlugin.getConfigurations()) {
            for (Configuration config : configurations) {
                if (config.equals(noteConfig)) {
                    notesConfiguration = (PlayableAnalyzableConfiguration) notesPlugin.getConfigurations().get(0);
                    return;
                }
            }
        }

        notesConfiguration = null;
    }

    private void stepChanged() {
        if (dialog.getCurrentStep() == 0) {
            dialog.disableBack();
            filesPane.removeAll();
            filesComboBoxes.clear();
            dialog.disableFinish();
            dialog.disableBack();
        } else if (dialog.getCurrentStep() == 1) {
            dialog.disableNext();
            dialog.enableBack();

            int row = 0;
            for (Configuration configuration : configurations) {
                if (!notesPlugin.getConfigurations().contains(configuration)) {
                if (configuration instanceof VisualizableConfiguration) {
                    filesPane.add(new JLabel(configuration.getId()), gbc.gy(row++));
                    VisualizableConfiguration c = (VisualizableConfiguration) configuration;
                    List<String> creators = c.getCompatibleCreators();
                    
                    files = findFilesCreatedBy(projectRoot, creators);
                    if (files.size() > 0) {
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
                } else if(configuration instanceof NotPlayableAnalyzableConfiguration) {
                    NotPlayableAnalyzableConfiguration c = (NotPlayableAnalyzableConfiguration) configuration;
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
            
        } 
        updateState();
    }

    public static void main(String[] args) {
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

    public List<Configuration> getConfigurations() {
        ArrayList<Configuration> list = new ArrayList<>();
        Configuration c;
        VisualizableConfiguration vc;
        PlayableAnalyzableConfiguration pac;
        NotPlayableAnalyzableConfiguration npac;
        for (JComboBox filesComboBox : filesComboBoxes) {
            FilePath f = (FilePath) filesComboBox.getSelectedItem();
            c = (Configuration) filesComboBox.getClientProperty("configuration");
            if (c instanceof PlayableAnalyzableConfiguration ) {
                pac = (PlayableAnalyzableConfiguration) c;
                pac.addFile(f.file);
                list.add(pac);
            } else if (c instanceof VisualizableConfiguration) {
                vc = (VisualizableConfiguration) c;
                vc.addFile(f.file);
                list.add(vc);
            } else {
                npac = (NotPlayableAnalyzableConfiguration) c;
                npac.addFile(f.file);
                list.add(npac);
            }
        }
        return list;
    }

    public List<PlayableAnalyzableConfiguration> getPlayableConfigurations() {
        ArrayList<PlayableAnalyzableConfiguration> list = new ArrayList<>();
        Configuration c;
        PlayableAnalyzableConfiguration pac;
        VisualizableConfiguration vc;
        NotesVisualization notesVisualization;

        for (JComboBox filesComboBox : filesComboBoxes) {
            FilePath f = (FilePath) filesComboBox.getSelectedItem();
            c = (Configuration) filesComboBox.getClientProperty("configuration");

            if (c instanceof PlayableAnalyzableConfiguration ) {
                pac = (PlayableAnalyzableConfiguration) c;
                pac.addFile(f.file);
                list.add(pac);
                notesVisualization = new NotesVisualization(f.file.getAbsolutePath(),pac.getClass().getName());
                if( notesConfiguration!=null){((NotesAnalysisConfig) notesConfiguration).addPlayable(notesVisualization);}
            }
        }

        return list;
    }

    public PlayableAnalyzableConfiguration getNotesConfiguration() {
        return notesConfiguration;
    }

    public List<NotPlayableAnalyzableConfiguration> getNotPlayableConfigurations() {
        ArrayList<NotPlayableAnalyzableConfiguration> list = new ArrayList<>();
        Configuration c;
        NotPlayableAnalyzableConfiguration npac;

        for (JComboBox filesComboBox : filesComboBoxes) {
            FilePath f = (FilePath) filesComboBox.getSelectedItem();
            c = (Configuration) filesComboBox.getClientProperty("configuration");

            if(c instanceof NotPlayableAnalyzableConfiguration) {
                npac = (NotPlayableAnalyzableConfiguration) c;
                npac.addFile(f.file);
                list.add(npac);
            }
        }
        return list;
    }

    public List<VisualizableConfiguration> getVisualizableConfigurations() {
        ArrayList<VisualizableConfiguration> list = new ArrayList<>();
        Configuration c;
        VisualizableConfiguration vc;
        NotesVisualization notesVisualization;
        for (JComboBox filesComboBox : filesComboBoxes) {
            FilePath f = (FilePath) filesComboBox.getSelectedItem();
            c = (Configuration) filesComboBox.getClientProperty("configuration");
            
            if(c instanceof PlayableAnalyzableConfiguration)
                continue;

            if (c instanceof VisualizableConfiguration) {
                vc = (VisualizableConfiguration) c;
                vc.addFile(f.file);
                list.add(vc);
                if (notesConfiguration != null) {
                    notesConfiguration.addFile(f.file);
                    notesVisualization = new NotesVisualization(f.file.getAbsolutePath(),vc.getClass().getName());
                    ((NotesAnalysisConfig) notesConfiguration).addVisualizable(notesVisualization);
                }
            }
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
