package mo.export.visualization;

import mo.organization.ProjectOrganization;
import mo.core.ui.WizardDialog;
import mo.organization.Participant;
import mo.organization.StageModule;
import mo.export.visualization.ExportProjectWizardPanel;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.GridBagLayout;
import java.io.File;
import java.nio.file.Files;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class ExportProjectDialog {
	WizardDialog dialog;
	JPanel selectionDataPanel;
	ExportProjectWizardPanel setLocationPanel;

	private JCheckBoxTree tree;
	private DefaultMutableTreeNode root;
	private ProjectOrganization organization;
	private TreeModel model;
	private List<File> originDataLocations;
	private HashMap destinationConfiguration;

	public ExportProjectDialog(ProjectOrganization organization) {
		this.organization = organization;

		loadTree();

		selectionDataPanel = new JPanel();
		selectionDataPanel.setName("Seleccionar los orígenes de datos");
        selectionDataPanel.setLayout(new BorderLayout());
        selectionDataPanel.setPreferredSize(new Dimension(500,500));
        selectionDataPanel.add(tree);

        tree.addCheckChangeEventListener(new JCheckBoxTree.CheckChangeEventListener() {
            public void checkStateChanged(JCheckBoxTree.CheckChangeEvent event) {
                TreePath[] paths = tree.getCheckedPaths();
            	originDataLocations = new ArrayList<>();
            	String relativePathToLeaf = "";
                for (TreePath tp : paths) {
                    for (Object pathPart : tp.getPath()) {
                    	if (((DefaultMutableTreeNode) pathPart).isLeaf()) {
                    		Object padre = ((DefaultMutableTreeNode) pathPart).getParent();
                    		relativePathToLeaf = padre.toString() + "/" + pathPart.toString() + "/";
                    		File file = new File(organization.getLocation(),relativePathToLeaf);
                    		if (Files.exists(file.toPath())) {
                    			originDataLocations.add(file);
                    		}
                    	}
                    }                   
                }
                updateState();
            }           
        });         

		dialog = new WizardDialog(null, "Export Project");
		dialog.setWarningMessage("Seleccione las fuentes de datos");
        dialog.addPanel(selectionDataPanel);
        setLocationPanel = new ExportProjectWizardPanel(dialog);
        dialog.addPanel(setLocationPanel);
        dialog.addActionListener(new WizardDialog.WizardListener() {
            @Override
            public void onStepChanged() {
                stepChanged();
            }
        });
	}

	private void updateState() {
        if (dialog.getCurrentStep() == 0) {
            if (originDataLocations.size() > 0) {
            	dialog.setWarningMessage("");
            	dialog.enableNext();
            } else {
            	dialog.disableNext();
            	dialog.setWarningMessage("Seleccione las fuentes de datos");
            }
        } else if (dialog.getCurrentStep() == 1) {
        }
    }

    private void stepChanged() {
    	if (dialog.getCurrentStep() == 0) {
    		dialog.disableBack();
    		if (originDataLocations.size() == 0) {
				dialog.setWarningMessage("Seleccione las fuentes de datos");
    		}
    	} else if (dialog.getCurrentStep() == 1) {
    		dialog.enableBack();
    		setLocationPanel.updateState();
    	}
    }

	private void loadTree() {
		tree = new JCheckBoxTree();
		root = new DefaultMutableTreeNode(organization.getLocation().getName());
		model = new DefaultTreeModel(root);

		File stageDir;
		File participantDir;
		DefaultMutableTreeNode participantNode;
		for (Participant participant : organization.getParticipants()) {
			participantDir = new File(organization.getLocation(), "participant-" + participant.id);
			participantNode = new DefaultMutableTreeNode("participant-" + participant.id);
			if (Files.exists(participantDir.toPath())) {
				root.add(participantNode);
			}

			DefaultMutableTreeNode stageNode;
			for (StageModule stage : organization.getStages() ) {
				stageDir = new File(organization.getLocation(), "participant-" + participant.id + "/" + stage.getCodeName().toLowerCase());
				if(Files.exists(stageDir.toPath())) {
					stageNode = new DefaultMutableTreeNode(new StageModuleTreeNode(stage));
					participantNode.add(stageNode);
				}
			}
		}
        tree.setModel(model);
	}

	public boolean show() {
		destinationConfiguration = dialog.showWizard();

        if (destinationConfiguration != null && originDataLocations.size() > 0) {
        	return true;
        }

        return false;
	}

	public File getExportRootFile() {
		File rootFolder = new File((String) destinationConfiguration.get("projectFolder"));
		return rootFolder;
	}

	public List<File> getOriginDataLocations() {
		return originDataLocations;
	}
}