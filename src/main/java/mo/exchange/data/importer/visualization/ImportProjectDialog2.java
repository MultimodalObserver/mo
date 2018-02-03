package mo.exchange.data.importer.visualization;

import mo.core.ui.WizardDialog;
import mo.core.filemanagement.project.ProjectUtils;
import mo.organization.ProjectOrganization;
import mo.organization.Participant;
import mo.organization.StageModule;
import mo.exchange.data.ExchangeableData;
import mo.exchange.data.visualization.ParticipantTreeNode;
import mo.exchange.data.visualization.StageTreeNode;
import mo.exchange.data.visualization.ExchangeableDataTree;
import mo.exchange.data.visualization.ExchangeableDataTree2;
import mo.exchange.data.visualization.LocationWizardPanel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import mo.exchange.data.ExchangeableProjectData;


public class ImportProjectDialog2
{
	WizardDialog dialog;
	JPanel projectPanel;
	JPanel dataTreePanel;
    LocationWizardPanel locationWizardPanel;

	private ProjectOrganization organization;
	private ExchangeableDataTree2 tree;
	private List<ExchangeableProjectData> data = new ArrayList();
	private JFileChooser chooser;
	private File selectedFile;
    private HashMap destinationConfiguration;

	public ImportProjectDialog2() {
        chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setControlButtonsAreShown(false);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || ProjectUtils.isProjectFolder(file);
            }

            @Override
            public String getDescription() {
                return "Carpetas y proyectos MO";
            }
        });

        chooser.addPropertyChangeListener(new PropertyChangeListener() {
		    public void propertyChange(PropertyChangeEvent evt) {
		        if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
		            JFileChooser chooser = (JFileChooser)evt.getSource();
		            selectedFile = (File)evt.getNewValue();
		            updateState();
		        }
		    }
		});

		projectPanel = new JPanel();
		projectPanel.setName("titulo");
        projectPanel.setLayout(new BorderLayout());
        projectPanel.setPreferredSize(new Dimension(650,500));
        projectPanel.add(chooser);

        tree = new ExchangeableDataTree2();
        tree.addExchangeableDataChangeEventListener(new ExchangeableDataTree2.ExchangeableDataChangeEventListener() {
            @Override
            public void onDataChange() {
                data = tree.getExchangeableData();
                updateState();
            }
        });

        dataTreePanel = new JPanel();
        dataTreePanel.setName("Seleccionar los datos");
        dataTreePanel.setLayout(new BorderLayout());
        dataTreePanel.setPreferredSize(new Dimension(650,500));

		dialog = new WizardDialog(null, "Import Project");
		dialog.setWarningMessage("Seleccione un proyecto de MO válido");
		dialog.addActionListener(new WizardDialog.WizardListener() {
            @Override
            public void onStepChanged() {
                stepChanged();
            }
        });

        locationWizardPanel = new LocationWizardPanel(dialog);

        dialog.addPanel(projectPanel);
        dialog.addPanel(dataTreePanel);
        dialog.addPanel(locationWizardPanel);
	}

	public boolean show() {
		destinationConfiguration = dialog.showWizard();

        if (destinationConfiguration != null) {
        	return true;
        }

        return true;
	}

    public File getOriginLocation() {
        File originLocation = organization.getLocation();
        return originLocation;
    }

    public File getDestinyLocation() {
        File destinyLocation = new File((String) destinationConfiguration.get("projectFolder"));
        return destinyLocation;
    }

    // here
    public List<ExchangeableProjectData> getData() {
        return data;
    }

	private void disableButtons() {
    	dialog.disableBack();
    	dialog.disableNext();
    	dialog.disableFinish();
    }

    private void updateDataTreePanel(File projectLocation) {
    	organization = new ProjectOrganization(projectLocation.getPath());
    	tree.setOrganization(organization);
    	tree.build();
    	dataTreePanel.removeAll();
    	dataTreePanel.add(tree);
    }

	private void updateState() {
		disableButtons();

		if (dialog.getCurrentStep() == 0) {
			if (ProjectUtils.isProjectFolder(selectedFile)) {
				updateDataTreePanel(selectedFile);
				dialog.enableNext();
				dialog.setWarningMessage("");
			} else {
				dialog.disableNext();
				dialog.setWarningMessage("Seleccione un proyecto de MO válido");
			}
		} else if (dialog.getCurrentStep() == 1) {
        	dialog.enableBack();

        	if (data.size() > 0) {
        		dialog.enableNext();
        		dialog.setWarningMessage("");
        	} else {
        		dialog.disableNext();
        		dialog.setWarningMessage("Elige los datos");
        	}
        } else if(dialog.getCurrentStep() == 2) {
        	dialog.enableBack();

            locationWizardPanel.updateState();
        }
	}

    private void stepChanged() {
    	updateState();
    }
}