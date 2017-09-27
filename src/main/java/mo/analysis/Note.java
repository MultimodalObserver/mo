package mo.analysis;

public class Note {
	private String comment;
	private int startTime;
	private int endTime;

	public Note(String comment, int startTime, int endTime) {
		this.comment = comment;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public int getEndTime() {
		return endTime;
	}
}