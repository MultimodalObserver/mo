package mo.capture.eeg;

public class EEGData {
    
    public long time;

    public ESense eSense;

    public EEGPower eegPower;
    
    /**
     * 0-200. -1 when is not set.
     */
    public short poorSignalLevel = -1;
    
    /**
     * 1-255. -1 when is not set.
     */
    public short blinkStrength = -1;

    public double mentalEffort;
    public boolean mentalEffortIsSet;

    public double familiarity;
    public boolean familiarityIsSet;

    public String status;

    public short rawEeg;
    public boolean rawEegIsSet;

    /**
     *
     * @param mentalEffort
     */
    public void setMentalEffort(double mentalEffort) {
        this.mentalEffort = mentalEffort;
        mentalEffortIsSet = true;
    }

    /**
     *
     * @param familiarity
     */
    public void setFamiliarity(double familiarity) {
        this.familiarity = familiarity;
        familiarityIsSet = true;
    }

    /**
     *
     * @param rawEeg
     */
    public void setRawEeg(short rawEeg) {
        this.rawEeg = rawEeg;
        rawEegIsSet = true;
    }

    @Override
    public String toString() {
        String s = "time:"+time+"\n"+
                "eSense:"+eSense +"\n" + 
                "eegPower:"+eegPower + "\n" +
                "ps:"+poorSignalLevel + "\n" + 
                "b:"+blinkStrength + "\n" +
                "effort:"+mentalEffort +" "+ mentalEffortIsSet+"\n" +
                "fam:"+familiarity + " " + familiarityIsSet +"\n"+
        "status: "+status+"\n"+
                "rawEeg:"+rawEeg+" "+rawEegIsSet;
        return s;
    }
}
