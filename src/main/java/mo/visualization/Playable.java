package mo.visualization;

public interface Playable {
    void pause();
    void seek(long millis);
    long getStart();
    long getEnd();
    void play(long millis);
}
