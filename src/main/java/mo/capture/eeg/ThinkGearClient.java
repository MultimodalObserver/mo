package mo.capture.eeg;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.Socket;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThinkGearClient {
    
    private final ArrayList<EEGListener> listeners;
    private final String host;
    private final Integer port;
    private final boolean enableRawOutput;
    private Socket socket;
    
    private static final Logger logger = Logger.getLogger(ThinkGearClient.class.getName());
    
    public ThinkGearClient(String host, Integer port, boolean enableRawOutput) throws IOException {
        
        listeners = new ArrayList<>();
        
        this.host = host == null ? "127.0.0.1": host;
        this.port = port == null ? 13854: port;
        
        this.enableRawOutput = enableRawOutput;
    }
    
    public void connect() throws IOException {
        socket = new Socket(host, port);
        
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        out.println("{enableRawOutput:" + enableRawOutput + ",format: \"Json\"}");
        
        out.println("{\"appName\":\"Multimodal Observer\",\"appKey\":\"9d3875a01fa7643b0618ae4618b7678a83124b4c\"}");
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        String line;
        while ( (line = in.readLine()) != null ) {
            long time = System.currentTimeMillis();
            
            try {
                EEGData data = mapper.readValue(line, EEGData.class);
                data.time = time;
                for (EEGListener listener : listeners) {
                    listener.onData(data);
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public void addEEGDataListener(EEGListener newListener) {
        if (!listeners.contains(newListener)) {
            listeners.add(newListener);
        }
    }
    
    public void removeEEGDataListener(EEGListener listenerToRemove) {
        listeners.remove(listenerToRemove);
    }
    
    public static void main(String[] args) throws IOException {
        ThinkGearClient client = new ThinkGearClient(null, null, false);
        client.addEEGDataListener(new EEGListener() {
            @Override
            public void onData(EEGData data) {
                System.out.println(data+"\n");
            }
        });
        client.connect();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
