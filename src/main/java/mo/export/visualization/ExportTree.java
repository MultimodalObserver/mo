package mo.export.visualization;

import mo.organization.ProjectOrganization;
import mo.organization.StageModule;
import mo.organization.Participant;
import java.io.File;
import java.nio.file.Files;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ExportTree extends JCheckBoxTree {
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	private ProjectOrganization organization;

	public ExportTree(ProjectOrganization organization) {
		super();
		this.organization = organization;
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
				if (Files.exists(stageDir.toPath())) {
					stageNode = new DefaultMutableTreeNode(new StageModuleTreeNode(stage));
					participantNode.add(stageNode);
				}
			}
		}
        this.setModel(model);
	}
}