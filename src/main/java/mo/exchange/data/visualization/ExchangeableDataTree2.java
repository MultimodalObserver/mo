package mo.exchange.data.visualization;

import mo.organization.ProjectOrganization;
import mo.organization.StageModule;
import mo.organization.Participant;
import mo.exchange.data.visualization.ParticipantTreeNode;
import mo.exchange.data.visualization.StageTreeNode2;
import mo.exchange.data.ExchangeableData;
import java.io.File;
import java.nio.file.Files;
import java.lang.IllegalArgumentException;
import java.util.List;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

// import javax.swing.tree.DefaultMutableTreeNode;
// import javax.swing.tree.TreeModel;
// import javax.swing.tree.DefaultTreeModel;

import mo.exchange.data.ExchangeableProjectData;

public class ExchangeableDataTree2 extends JCheckBoxTree
{
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	private ProjectOrganization organization;
	private String filter = "PARTICIPANTS_AND_STAGES";
	private List<ExchangeableProjectData> data = new ArrayList();
	private List<ExchangeableDataChangeEventListener> listeners = new ArrayList();

	public static String PARTICIPANTS_ONLY = "PARTICIPANTS_ONLY";
	public static String PARTICIPANTS_AND_STAGES = "PARTICIPANTS_AND_STAGES";

	public ExchangeableDataTree2() {
		super();

        this.addCheckChangeEventListener(new JCheckBoxTree.CheckChangeEventListener() {
            public void checkStateChanged(JCheckBoxTree.CheckChangeEvent event) {
                TreePath[] paths = getCheckedPaths();
            	String relativePathToLeaf = "";
            	data = new ArrayList();

            	Object[] pathelEments;
				DefaultMutableTreeNode parentNode, childNode;
				Object nodeContent;
				ParticipantTreeNode pNode;
				StageTreeNode2 sNode;
				Participant participant;
				String stage;
				ExchangeableProjectData dataElement;
				List<StageModule> stages;
				List<String> stageFolders;
				int n;

                for (TreePath tp : paths) {
                	pathelEments = tp.getPath();
                	if (pathelEments.length == 2) {
                		for (Object element : pathelEments) {
                			parentNode = (DefaultMutableTreeNode) element;
                			nodeContent = parentNode.getUserObject();
                			if (nodeContent instanceof ParticipantTreeNode) {
                				pNode = (ParticipantTreeNode) nodeContent;
                				participant = pNode.getParticipant();
                				dataElement = new ExchangeableProjectData();
                				dataElement.participant = participant;
                				n = getModel().getChildCount(parentNode);
                				stages = new ArrayList();
                				stageFolders = new ArrayList();
                				for (int i = 0; i < n ; i++) {
                					childNode = (DefaultMutableTreeNode) getModel().getChild(parentNode,i);
                					sNode = (StageTreeNode2) childNode.getUserObject();
                					stage = sNode.getStage();
                					// stages.add(stage);
                					stageFolders.add(stage);
                				}
                				dataElement.stageFolders = stageFolders;
                				data.add(dataElement);
                			}
                		}
                	}
                }
                fireExchangeableDataChangeListener();
            }
        });
	}

	public void setOrganization(ProjectOrganization organization) {
		this.organization = organization;
	}

	public void setExchangeableDataFilter(String filter) throws IllegalArgumentException { 
		if (filter.equals(PARTICIPANTS_ONLY) || filter.equals(PARTICIPANTS_AND_STAGES)) {
			this.filter = filter;
		} else {
			throw new IllegalArgumentException("Opción inválida");
		}
	}

	private void buildParticipants() {
		File stageDir;
		File participantDir;
		DefaultMutableTreeNode participantNode;
		for (Participant participant : organization.getParticipants()) {
			System.out.println("la carpeta = " + participant.folder);
			participantDir = new File(organization.getLocation(), "participant-" + participant.id);
			participantNode = new DefaultMutableTreeNode("participant-" + participant.id);
			if (Files.exists(participantDir.toPath())) {
				root.add(participantNode);
			}
		}
        this.setModel(model);
	}

	private void buildParticipantsAndStages() {
		File stageDir;
		File participantDir;
		DefaultMutableTreeNode participantNode;
		for (Participant participant : organization.getParticipants()) {
			System.out.println("la carpeta = " + participant.folder);
			participantDir = new File(organization.getLocation(), "participant-" + participant.id);
			participantNode = new DefaultMutableTreeNode(new ParticipantTreeNode(participant));
			if (Files.exists(participantDir.toPath())) {
				root.add(participantNode);
			}

			DefaultMutableTreeNode stageNode;
			for (StageModule stage : organization.getStages() ) {
				stageDir = new File(organization.getLocation(), "participant-" + participant.id + "/" + stage.getCodeName().toLowerCase());
				if (Files.exists(stageDir.toPath())) {
					stageNode = new DefaultMutableTreeNode(new StageTreeNode2(stage.getCodeName()));
					participantNode.add(stageNode);
				}
			}
		}
        this.setModel(model);
	}

	public void build() {
		root = new DefaultMutableTreeNode(organization.getLocation().getName());
		model = new DefaultTreeModel(root);

		if (filter.equals(PARTICIPANTS_ONLY)) {
			buildParticipants();
		} else if(filter.equals(PARTICIPANTS_AND_STAGES)) {
			buildParticipantsAndStages();
		}
	}

	public List<ExchangeableProjectData> getExchangeableData() {
		return data;
	}

	public List<Participant> getParticipants() {
		List<Participant> participants = new ArrayList();

		// for (ExchangeableData element : data) {
		// 	participants.add(element.participant);
		// }

		return participants;
	}

	public void addExchangeableDataChangeEventListener(ExchangeableDataChangeEventListener toAdd) {
		listeners.add(toAdd);
	}

	private void fireExchangeableDataChangeListener() {
		for (ExchangeableDataChangeEventListener listener : listeners) {
			listener.onDataChange();
		}
	}

	public interface ExchangeableDataChangeEventListener {
		void onDataChange();
	}
}