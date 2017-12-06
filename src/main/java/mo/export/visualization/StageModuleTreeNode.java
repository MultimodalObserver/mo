package mo.export.visualization;

import mo.organization.StageModule;

public class StageModuleTreeNode {
	private StageModule stage;

	public StageModuleTreeNode(StageModule stage) {
		this.stage = stage;
	}

	public StageModule getStage() {
		return stage;
	}

	public void setStage(StageModule stage) {
		this.stage = stage;
	}

	@Override
	public String toString() {
		return stage.getCodeName();
	}
}