package mo.eeg.capture;

public class ESense {
    public byte attention;
    public byte meditation;

    @Override
    public String toString() {
        return "att:"+attention+" med:"+meditation;
    }
    
}
