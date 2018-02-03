package mo.export;

import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.organization.ProjectOrganization;
import mo.export.plugin.ExportDataPlugin;
import mo.export.plugin.ExportPlugin;
import mo.export.visualization.ExportDataDialog;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.Dialog.ModalityType;
import javax.swing.SwingWorker;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;

public class ExportDataManager {
	
    private ProjectOrganization organization;
    private ExportModule module;
    private boolean accepted;
    private boolean cancelledExport;
    private List<Plugin> plugins;
    private List<ExportDataPlugin> exportDataPlugins;
    private List<String> compatibleCreators;
    private JLabel label;
    private JButton cancelButon;
    private JButton okButon;
    private JDialog waitDialog;
    private SwingWorker exportWorker;
    private SwingWorker cancelWorker;

    public final static Logger logger = Logger.getLogger(ExportProjectManager.class.getName());

	public ExportDataManager(File folderProject) {
        this.organization = new ProjectOrganization(folderProject.getPath());
        cancelledExport = false;
        exportDataPlugins = new ArrayList();

        plugins = PluginRegistry.getInstance().getPluginsFor("mo.export.plugin.ExportDataPlugin"); 
        ExportDataPlugin dataPlugin;
        for (Plugin plugin : plugins) {
            dataPlugin = (ExportDataPlugin) plugin.getNewInstance();
            exportDataPlugins.add(dataPlugin);
        }

        newExportConfiguration();
	}

    public void newExportConfiguration() {
        ExportDataDialog dialog = new ExportDataDialog(organization, exportDataPlugins);
        boolean accept = dialog.show();

        if (accept) {
            List<ExportPlugin> selectedPlugins = (List<ExportPlugin>) (Object) dialog.getSelectedPlugins();
            module = new ExportModule(selectedPlugins); 
            module.setOrganization(organization);
            module.setDataSourcesLocations(dialog.getOriginDataLocations());
            module.setExportLocationRoot(dialog.getExportRootFile());

            export();
            showDialog();
        }
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
            cancelledExport = true;
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
                    if (SwingWorker.StateValue.DONE.equals(evt.getNewValue()) && !cancelledExport) {
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
                    cancelledExport = false;
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
                        okButon.setEnabled(true);
                    }
                }
            }
        );

        label.setText("cancelando...");
        cancelButon.setEnabled(false);
        okButon.setEnabled(false);

        cancelWorker.execute();
    }
}