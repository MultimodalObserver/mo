package mo.analysis;

import mo.visualization.Playable;

public class NotesPlayer implements Playable {

    private long start;
    private long end;

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }
	@Override
	public long getStart() {
		return start;
	}

	@Override
    public long getEnd() {
    	return end;
    }

    @Override
    public void play(long millis) {

    }

	@Override
    public void stop() {

    }

    @Override
    public void seek(long millis) {

    }

    @Override
    public void pause() {

    }
}