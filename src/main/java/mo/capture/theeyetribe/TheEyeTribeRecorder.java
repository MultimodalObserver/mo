package mo.capture.theeyetribe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;
import com.theeyetribe.clientsdk.data.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.capture.eeg.EEGRecorder;
import mo.organization.FileDescription;

public class TheEyeTribeRecorder implements IGazeListener {

    private final TETClient client;
    private TheEyeTribeConfiguration config;

    private static final Logger logger = Logger.getLogger(TheEyeTribeRecorder.class.getName());
    private File output;
    private FileOutputStream outputStream;
    private FileDescription desc;
    ObjectMapper mapper = new ObjectMapper();

    public TheEyeTribeRecorder(File stageFolder, TheEyeTribeConfiguration aThis) {
        client = new TETClient(null, null);

        this.config = config;
        createFile(stageFolder);
    }

    private void createFile(File parent) {

        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HH.mm.ss.SSS");

        String reportDate = df.format(now);

        output = new File(parent, reportDate + "_" + config.getId() + ".txt");
        try {
            output.createNewFile();
            outputStream = new FileOutputStream(output);
            desc = new FileDescription(output, TheEyeTribeRecorder.class.getName());
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    private void deleteFile() {
        if (output.isFile()) {
            output.delete();
        }
        if (desc.getDescriptionFile().isFile()) {
            desc.deleteFileDescription();
        }
    }

    public void cancel() {
        stop();
        deleteFile();
    }

    public void start() {
        client.addGazeListener(this);
    }

    public void pause() {
        client.removeGazeListener(this);
    }

    public void resume() {
        client.addGazeListener(this);
    }

    public void stop() {
        client.removeGazeListener(this);
        client.disconnect();
        try {
            outputStream.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onGazeUpdate(GazeData gd) {
        try {
            outputStream.write((gazeDataToJson(gd)+"\n").getBytes());
        } catch (IOException ex) {
            Logger.getLogger(EEGRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static String gazeDataToJson(GazeData gd) {
        String json = "{";
        
        //time
        json += "\"time\":" + gd.timeStamp + ",";
        
        //timestamp
        json += "\"timestamp\":\"" + gd.timeStampString + "\",";
        
        //state
        json += "\"state\":" + gd.state + ",";
        
        //fix
        json += "\"fix\":" + gd.isFixated + ",";
        
        // avg
        if (gd.hasSmoothedGazeCoordinates()) {
            json += "\"avg\":" + pointToJson(gd.smoothedCoordinates) + ",";
        }
        
        //raw
        if (gd.hasRawGazeCoordinates()) {
            json += "\"raw\":" + pointToJson(gd.rawCoordinates) + ",";
        }

        //lefteye
        json += "\"lefteye\":{";
        if (gd.hasSmoothedGazeCoordinates()) {
            json += "\"avg\":" + pointToJson(gd.leftEye.smoothedCoordinates) + ",";
        }
        json += "\"pcenter\":" + pointToJson(gd.leftEye.pupilCenterCoordinates) + ",";
        json += "\"psize\":" + gd.leftEye.pupilSize;
        if (gd.hasRawGazeCoordinates()) {
            json += ",\"raw\":" +pointToJson(gd.leftEye.rawCoordinates);
        }
        json += "},";

        //righteye
        json += "\"righteye\":{";
        if (gd.hasSmoothedGazeCoordinates()) {
            json += "\"avg\":" + pointToJson(gd.rightEye.smoothedCoordinates) + ",";
        }
        json += "\"pcenter\":" + pointToJson(gd.rightEye.pupilCenterCoordinates) + ",";
        json += "\"psize\":" + gd.rightEye.pupilSize;
        if (gd.hasRawGazeCoordinates()) {
            json += ",\"raw\":" +pointToJson(gd.rightEye.rawCoordinates);
        }
        json += "}";

        json += "}";
                
        return json;
    }
    
    private static String pointToJson(Point2D point) {
        return "{\"x\":" + point.x + ",\"y\":" + point.y + "}";
    }
    
    public static void main(String[] args) {
        System.out.println(gazeDataToJson(new GazeData()));
    }
}
