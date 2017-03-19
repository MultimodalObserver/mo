package mo.capture.theeyetribe;

import com.theeyetribe.clientsdk.GazeManager;
import static com.theeyetribe.clientsdk.GazeManager.ApiVersion;
import static com.theeyetribe.clientsdk.GazeManager.ClientMode;
import com.theeyetribe.clientsdk.IConnectionStateListener;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import java.util.ArrayList;
import java.util.logging.Logger;

public class TETClient implements IConnectionStateListener, IGazeListener {

    String hostname;
    Integer port;
    GazeManager gm;
    ArrayList<IGazeListener> listeners;
    
    private static final Logger logger = Logger.getLogger(TETClient.class.getName());

    public TETClient(String hostname, Integer port) {

        this.hostname = hostname == null ? "127.0.0.1" : hostname;
        this.port = port == null ? 6555 : port;
        
        listeners = new ArrayList<>();
    }
    
    public void addGazeListener(IGazeListener listener) {
        listeners.add(listener);
    }
    
    public void removeGazeListener(IGazeListener listener) {
        listeners.remove(listener);
    }

    public void disconnect() {
        TETClient t = this;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                gm.removeGazeListener(t);
                gm.deactivate();
            }
        });
    }

    public void connect() {
        gm = GazeManager.getInstance();
        boolean success = gm.activate(ApiVersion.VERSION_1_0, ClientMode.PUSH, hostname, port, this);

        if (!success) {
            logger.info("Can't connect to TheEyeTribe server");
        }
        System.out.println("Success: " + success);
        
        gm.addGazeListener(this);
        
        System.out.println(gm.getNumGazeListeners());
        System.out.println("ya");
    }

    public static void main(String[] args) {
        TETClient t = new TETClient(null, null);
        t.connect();
        System.out.println("asd");
    }

    @Override
    public void onConnectionStateChanged(boolean bln) {
        System.out.println("changed :" + bln);
        System.out.println(gm.getTrackerState().name());
    }

    @Override
    public void onGazeUpdate(GazeData gd) {
        for (IGazeListener listener : listeners) {
            listener.onGazeUpdate(gd);
        }
        System.out.println(gazeDataToString(gd));
    }  
    
    private String gazeDataToString(GazeData gd) {
        String r = "";
        
        r += gd.timeStampString + " " + gd.timeStamp + " " + gd.state + ":" + gd.stateToString() +
                " " + gd.isFixated + " " + gd.hasRawGazeCoordinates()+ ":" + gd.rawCoordinates + " " +
                gd.hasSmoothedGazeCoordinates() + ":" + gd.smoothedCoordinates + " " +
                gd.leftEye + " " + gd.rightEye;
        
        return r;
    }
}
