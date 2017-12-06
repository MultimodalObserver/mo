package mo.export;

import mo.organization.ProjectOrganization;
import mo.organization.Participant;
import mo.organization.StageModule;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.export.plugin.ExportPlugin;
import mo.export.plugin.ExportProjectPlugin;
import mo.export.visualization.ExportProjectDialog;
import java.io.File;
import java.nio.file.Files;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.Dialog.ModalityType;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.UIManager;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ExportProjectManager {
    public final static Logger logger = Logger.getLogger(ExportProjectManager.class.getName());

    private List<ExportPlugin> plugins;
    private ExportPlugin plugin;
    private ProjectOrganization organization;
    private File folderProject;
    private JTree tree;
    private SwingWorker<Void, Void> mySwingWorker;
    private Thread thread;
    private JDialog waitDialog;
    private JButton cancelButon;
    private JButton okButon;
    private JLabel label;
    private boolean accepted;
    private boolean cancelled;
    private List<ExportProjectPlugin> exportPlugins;
    private SwingWorker exportWorker;
    private SwingWorker cancelWorker;
    private ExportModule module;

	public ExportProjectManager() {
        cancelled = false;
        List<Plugin> plugins = PluginRegistry.getInstance().getPluginsFor("mo.export.plugin.ExportProjectPlugin");
        exportPlugins = new ArrayList();
        ExportProjectPlugin p;
        for (Plugin plugin : plugins) {
            p = (ExportProjectPlugin) plugin.getNewInstance();
            exportPlugins.add(p);
        }
	}

    public List<ExportProjectPlugin> getPlugins() {
        return exportPlugins;
    }

    public void newExportConfiguration(ExportProjectPlugin plugin, File folderProject) {
        this.plugin = plugin;

        ProjectOrganization organization = new ProjectOrganization(folderProject.getPath());

        ExportProjectDialog exportDialog = new ExportProjectDialog(organization);
        boolean accept = exportDialog.show();
        if (accept) {
            module = new ExportModule(Arrays.asList(plugin)); 
            module.setOrganization(organization);
            module.setDataSourcesLocations(exportDialog.getOriginDataLocations());
            module.setExportLocationRoot(exportDialog.getExportRootFile());

            export();

            showDialog();
        }
    }

    public void done() {
        label.setText("Exportación lista");
        waitDialog.setTitle("Done.");
        okButon.setEnabled(true);
        cancelButon.setEnabled(false);
    }

    private void setOrganization(ProjectOrganization organization) {
        this.organization = organization;
    }

    private void export() {
        exportWorker = new SwingWorker<Void,Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    module.export();
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };

        exportWorker.addPropertyChangeListener(
            new PropertyChangeListener() {
                public  void propertyChange(PropertyChangeEvent evt) {
                    if (SwingWorker.StateValue.DONE.equals(evt.getNewValue()) && !cancelled) {
                        label.setText("Exportacion lista");
                        cancelButon.setEnabled(false);
                        okButon.setEnabled(true );
                    }
                }
            }
        );

        exportWorker.execute();
    }

    private void cancelExport() {
        exportWorker.cancel(true);

        cancelWorker = new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    cancelled = false;
                    module.cancelExport();
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };

        cancelWorker.addPropertyChangeListener(
            new PropertyChangeListener() {
                public  void propertyChange(PropertyChangeEvent evt) {
                    if (SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                        label.setText("Cancelacion lista");
                        cancelButon.setEnabled(false);
                        okButon.setEnabled(true );
                    }
                }
            }
        );

        label.setText("cancelandors");
        cancelButon.setEnabled(false);
        okButon.setEnabled(false);

        cancelWorker.execute();
    }

    private void showDialog() {
        waitDialog = new JDialog();

        label = new JLabel("Exportando, por favor espere.");

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel();

        cancelButon = new JButton("Cancelar");
        cancelButon.setEnabled(true);
        cancelButon.addActionListener((ActionEvent e) -> {
            accepted = false;
            cancelled = true;
            cancelExport();
        });
        
        okButon = new JButton("Aceptar");
        okButon.setEnabled(false);
        okButon.addActionListener((ActionEvent e) -> {
            accepted = true;
            waitDialog.setVisible(false);
            waitDialog.dispose();

        });

        buttonsPanel.add(okButon);
        buttonsPanel.add(cancelButon);

        waitDialog.setModalityType(ModalityType.APPLICATION_MODAL);
        waitDialog.setLocationRelativeTo(null);
        waitDialog.setTitle("Please Wait...");

        panel.add(label, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        waitDialog.add(panel);

        waitDialog.pack();
        waitDialog.setVisible(true);
    }
}