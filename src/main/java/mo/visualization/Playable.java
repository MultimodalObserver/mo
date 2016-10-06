package mo.visualization;

public interface Playable {
    void setSpeed(double factor);
    void pause();
    void seek(long millis);
    long getStart();
    long getEnd();
    void play();
}
