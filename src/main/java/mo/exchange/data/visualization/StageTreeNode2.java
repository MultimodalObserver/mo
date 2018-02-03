package mo.exchange.data.visualization;

import mo.organization.StageModule;

public class StageTreeNode2
{
	private String stage;

	public StageTreeNode2(String stage) {
		this.stage = stage;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	@Override
	public String toString() {
		return stage;
	}
}