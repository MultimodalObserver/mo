package mo.exchange.data.visualization;

import mo.organization.StageModule;

public class StageTreeNode
{
	private StageModule stage;

	public StageTreeNode(StageModule stage) {
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