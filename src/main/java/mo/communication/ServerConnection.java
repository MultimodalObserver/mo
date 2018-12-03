package mo.communication;

import java.io.File;
import mo.communication.chat.ChatWindow;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.analysis.NotesConfigDialog;
import mo.capture.CaptureProvider;
import mo.capture.RecordableConfiguration;
import mo.communication.chat.ChatEntry;
import mo.communication.notes.RemoteNotes;
import mo.communication.streaming.capture.CaptureConfig;
import mo.communication.streaming.capture.CaptureEvent;
import mo.communication.streaming.capture.PluginCaptureListener;
import mo.communication.streaming.capture.PluginCaptureSender;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.organization.Configuration;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;
import mo.organization.StagePlugin;
import java.util.concurrent.CountDownLatch;

public class ServerConnection implements PluginCaptureListener,ConnectionSender, ConnectionListener{
    private ArrayList<RemoteClient> clients; //clients online
    //private static HashMap<String,PluginCaptureSender> availablePlugins; // id y PluginCaptureSender
    private HashMap<String,Object> availablePlugins;
    private ArrayList<ConnectionListener> listeners;
    private boolean isOnline;
    private boolean isStreaming;
    private String localIP = null;
    private final String multicastIP = "230.0.0.0";
    private int portTCP = 4444;
    private int portUDP = 5555;
    private ServerUDP serverUDP;
    private ServerTCP serverTCP;
    private HashMap<String,Integer> ports;

    private ProjectOrganization org;
    private Participant participant;
    private File storageFolder;

    private final CountDownLatch latch = new CountDownLatch(1);
      
    private static ServerConnection connection;
    private ServerConnection(){
        try {
            localIP = InetAddress.getLocalHost().getHostAddress();
            clients = new ArrayList<>();
            availablePlugins = new HashMap<>();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.println("Cerrando con "+Thread.activeCount()+" hebras");
                        downServer();
                        //latch.countDown();
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Runtime.getRuntime().halt(0);
                }
            }));
        } catch (UnknownHostException ex) {}
    }

    public static ServerConnection getInstance(){
        if(connection == null){
            connection = new ServerConnection();
        }
        return connection;
    }

    ArrayList<CommunicationConfiguration> configsPluginsTCP;
    ArrayList<CommunicationProvider1> pluginsTCP;
    // activa el servidor con la configuración de puertos ingresadas por el usuario (retorna true)
    public boolean upServer(HashMap<String,Object> configuration){
        try {
            portUDP = Integer.parseInt((String) configuration.get("portUDP"));
            portTCP = Integer.parseInt((String) configuration.get("portTCP"));
            
            serverTCP = new ServerTCP(portTCP);
            serverUDP = new ServerUDP(multicastIP, portUDP,2048);
            
            if(configuration.containsKey("configDirectDevs")){
                // configurar dispositivos configDirectDevs
                HashMap<String,Object> devConf = (HashMap)configuration.get("configDirectDevs");
                configureDirectDevices(devConf);
                
            }
            
            //chat = new ChatWindow(null);
            
            configsPluginsTCP = new ArrayList<>();
            pluginsTCP = new ArrayList<>();

            for (Plugin plugin : PluginRegistry.getInstance().getPluginData().getPluginsFor("mo.communication.CommunicationProvider1")) {
                CommunicationProvider1 c = (CommunicationProvider1) plugin.getNewInstance();
                pluginsTCP.add(c);
            }

            for(CommunicationProvider1 plugin: pluginsTCP){
                CommunicationConfiguration config = plugin.initNewConfiguration(null);
                configsPluginsTCP.add(config);
                if(config instanceof ConnectionListener){
                    ServerConnection.getInstance().subscribeListener((ConnectionListener) config);
                }
                if(config instanceof ConnectionSender){
                    ((ConnectionSender) config).subscribeListener(this);
                    System.out.println("Se suscribió "+config.toString()+" a Connection");
                }
                config.showPlayer();
                System.out.println(config.toString()+ " isntancido en UPSERVER");
            }
            
            
//            if(participant != null)
//                chat.setName(participant.name);
//            if(remoteNotes == null)
//                remoteNotes = new RemoteNotes();
            this.isOnline = true;
            //prepareDirectDevices();
            return true;
        } catch (IOException ex) {
            try {
                serverTCP.endConnection();
                serverUDP.endConnection();
            } catch (IOException ex1) {}
        }
        return false;
    }
    
    public void downServer() throws ClassNotFoundException{
        System.out.println("DownServer ejecutando");
        System.out.println("online = "+isOnline);
        if(isOnline){
            System.out.println("está online");
            PetitionResponse response = new PetitionResponse(Command.END_CONNECTION,null);
            for(RemoteClient rc: clients){
                System.out.println("cerrando cliente "+rc);
                try {
                    rc.send(response);
                    rc.endConnection();
                    rc = null;
                } catch (IOException ex) {}
            }
                new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        serverTCP.endConnection();
                    } catch (IOException ex) {
                        System.out.println("exception en downServer");
                    }
                    clients.removeAll(clients);
                    serverTCP = null;
                    serverUDP.endConnection();
                    serverUDP = null;
                    for(CommunicationConfiguration config: configsPluginsTCP){
                        config.closePlayer();
                    }
                    configsPluginsTCP.clear();
                    //listeners.clear(); ver cómo no eliminar al módulo de captura
                    for(Map.Entry<String, StreamableConfiguration> entry : directPluginsConfigs.entrySet()) {
                        entry.getValue().stopStreaming();
                    }
                    directPluginsConfigs.clear();
                    isOnline = false;
                }
            }).start();
                
            
        }
        System.out.println("salió de downServer");
    }
    
    public ServerConnection setPortUDP(int port){
        this.portUDP = port;
        return this;
    }
    
    public ServerConnection setPortTCP(int port){
        this.portTCP = port;
        return this;
    }
    
    public int getPortUDP(){
        return this.portUDP;
    }
    
    public int getPortTCP(){
        return this.portTCP;
    }
    
    public boolean getConnectionState(){
        return isOnline;
    }
    
    public void setParticipantInfo(ProjectOrganization org, Participant participant,File storageFolder){
        this.org = org;
        this.participant = participant;
        this.storageFolder = storageFolder;
//        if(chat != null){
//            chat.setName(participant.name);
//            chat.setInfo(storageFolder);
//        }
        for(CommunicationConfiguration config: configsPluginsTCP){
            config.setInfo(storageFolder, participant.name);
            System.out.println(config.toString()+ " configurado en setParticipantInfo");
        }
        //this storageFolder = storageFolder;
    }

    // para agregar los plugins de captura activos que pueden ser transmitidos (se ejecuta al iniciar una captura)
    public void addActiveCapturePlugin(PluginCaptureSender pcs){
        if(availablePlugins == null)
            availablePlugins = new HashMap<>();
        String configID = ((RecordableConfiguration) pcs).getId(); // nombre configuración plugin captura
        HashMap<String,Object> config = new HashMap<>(); // información adicional
        config.put("sender", pcs); // PluginCaptureSender
        availablePlugins.put(configID, config);
        pcs.subscribeListener((PluginCaptureListener) this);
        for(RemoteClient rc: clients){
            rc.addCapturePlugin(configID);
        }
        System.out.println("hay "+availablePlugins.size()+" plugins de capture que pueden ser transmitidos");
    }

    // para agregar listeners que escuchen a conexión, por ejemplo, el módulo de captura para pausar remotamente
    public void addListener(ConnectionListener c){
        if(listeners == null)
            listeners = new ArrayList<>();
        listeners.add(c);
    }

    // handler de datos que provendan de los plugins de captura
    @Override
    public void onDataReceived(Object obj,CaptureEvent e){
        // podría agregar un argmento ID para identificar el emisor
        //System.out.println("llegó: "+obj+" y "+e);
        if(clients != null && !clients.isEmpty() && availablePlugins != null && !availablePlugins.isEmpty()){
            HashMap<String,Object> map = new HashMap<>();
            map.put("data", e);
            serverUDP.send(new PetitionResponse(Command.DATA_STREAMING,map));
        }
    }
    
    @Override
    public void setInitConfiguration(Object obj, CaptureConfig cc) {
        if(availablePlugins.containsKey(cc.getConfigID())){
            HashMap config = (HashMap)availablePlugins.get(cc.getConfigID());
            if(!config.containsKey("initialConfig"))
                config.put("initialConfig", cc);
        }
    }

    // espera clientes TCP
    public void waitForClients(){
        if(!isOnline) return;
        RemoteClient rc = new RemoteClient();
        new Thread(()->{
            try{
                if(isOnline){
                    if(serverTCP.accept(rc)){
                        clients.add(rc);
                        listeningTCP(rc);
                        initStreaming();
                        sendDirectConfigs(rc);
                        if(!availablePlugins.isEmpty()){
                            sendInitialConfigs(rc);
                            for(Map.Entry<String,Object> entry: availablePlugins.entrySet()){
                                rc.addCapturePlugin(entry.getKey());
                            }
                        }
                        if(isOnline){
                            waitForClients();
                        }
                    }
                }
            }catch(SocketException | NullPointerException e){
                isOnline = false;
            } catch (IOException ex) {
                Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }
    
    public void sendInitialConfigs(RemoteClient rc){
        HashMap<String, Object> configs = new HashMap<>();
        availablePlugins.forEach((k,v) -> {
            configs.put(k, ((HashMap)v).get("initialConfig"));
        });
        ArrayList<RemoteClient> inactiveClients = new ArrayList<>();
        PetitionResponse response = new PetitionResponse(Command.GET_ACTIVE_PLUGINS_RESPONSE,configs);
        boolean sent = false;
        if(rc != null){
            sent = rc.send(response);
            if(!sent)
                inactiveClients.add(rc);
        }
        else{
            for(RemoteClient client: clients){
                sent = client.send(response);
                if(!sent)
                    inactiveClients.add(client);
            }
            
        }
        clients.removeAll(inactiveClients);
        
        /*
        BORRAR ESTO DESPUES DE LISTO -------- BORRAR -------- BORRAR --------- BORRAR --------- BORRAR

        */
//        List<CommunicationProvider> plugins;
//        plugins = new ArrayList<>();
//        for (Plugin plugin : PluginRegistry.getInstance().getPluginData().getPluginsFor("mo.communication.CommunicationProvider")) {
//            CommunicationProvider c = (CommunicationProvider) plugin.getNewInstance();
//            plugins.add(c);
//            new Thread(()->c.initNewConfiguration(null, null)).start();
//        }
        
    }
    
    public void listeningTCP(RemoteClient rc){
        new Thread(() -> {
            while(true){
                try {
                    PetitionResponse petition = serverTCP.receive(rc.getSocket());
                    if(petition != null){
                        if(petition.getType().equals(Command.END_CONNECTION)){
                            return;
                        }
                        new Thread(() -> {
                            try {
                                handlerTCP(rc,petition);
                            } catch (IOException ex) {}
                            catch (ClassNotFoundException ex) {}
                        }).start();
                    }
                } catch (IOException ex) {}
                  catch (NullPointerException ex) { isOnline = false;}
            }
        }).start();
    }
    
    private void handlerTCP(RemoteClient rc, PetitionResponse p) throws IOException, ClassNotFoundException{
        PetitionResponse r;
        boolean sent = false;
        ArrayList<RemoteClient> inactiveClients = new ArrayList<>();
        switch (p.getType()) {
            case Command.GET_ACTIVE_PLUGINS:
                System.out.println("ESTA PIDIENDO LOS PLUGINS");
                break;
                
            case Command.GET_PORTS:
                HashMap<String, Object> map = new HashMap<>();
                map.put("portUDP", ((Integer)portUDP).toString());
                r = new PetitionResponse(Command.GET_PORTS_RESPONSE, map);
                sent = rc.send(r);
                if(!sent)
                    clients.remove(rc);
                break;
                /*
                    NOTAS Y CHAT
                */
            case Command.MSG_CLIENT_TO_SERVER:
                System.out.println("CHATEANDO");
                notifyListeners(this,p);
                r = new PetitionResponse(Command.MSG_SERVER_TO_CLIENT, p.getHashMap());
                
                for(RemoteClient client: clients){
                    if(!client.equals(rc)){
                        sent = client.send(r);
                        if(!sent)
                            inactiveClients.add(client);
                    }
                }
                removeClients(inactiveClients);
                break;
                
            case Command.MSG_SERVER_TO_CLIENT:
                r = new PetitionResponse(Command.MSG_SERVER_TO_CLIENT, p.getHashMap());
                
                for(RemoteClient client: clients){
                    sent = client.send(r);
                    if(!sent)
                        inactiveClients.add(client);
                }
                removeClients(inactiveClients);
                break;

            case Command.UPDATE_STATE_RECORDING:
                for(RemoteClient client: clients){
                    sent = client.send(p);
                    if(!sent)
                        inactiveClients.add(client);
                }
                removeClients(inactiveClients);
                break;
                
            case Command.END_CONNECTION:
                inactiveClients.add(rc);
                rc.endConnection();
                removeClients(inactiveClients);
                break;
                
            case Command.UPDATE_STATE_STREAMING:
                System.out.println("Se actualiza el % de streaming con "+p);
                for(RemoteClient client: clients){
                    sent = client.send(p);
                    if(!sent)
                        inactiveClients.add(client);
                }
                removeClients(inactiveClients);
                break;
                
            case Command.STOP_STREAMING:
                String idPlugin = p.getHashMap().get("id").toString();
                boolean contains = false;
                rc.removePlugin(idPlugin);
                for(RemoteClient client : clients){
                    if(client.getCapturePlugins().contains(idPlugin)){
                        contains = true;
                    }
                }
                if(!contains){
                    //si ningun cliente lo está visualizando, se notifica para que se detenga la emisión
                    notifyListeners(this,p);
                }
                break;
                
            case Command.CHANGE_QUALITY_STREAMING:
                rc.addCapturePlugin(p.getHashMap().get("id").toString());
                notifyListeners(this,p);
                break;
                
            default:
                System.out.println("se notifica  "+p);
                notifyListeners(this,p);
        }
    }
    
    public void notifyListeners(Object obj, PetitionResponse pr){
        if(listeners != null){
            for(ConnectionListener listener: listeners){
                listener.onMessageReceived(obj, pr);
            }
        }
    }

    public String getLocalIP() {
        return localIP;
    }

    public ArrayList<RemoteClient> getClients() {
        return this.clients;
    }
    
    public void removeClients(ArrayList<RemoteClient> inactiveClients){
        clients.removeAll(inactiveClients);
    }

    private HashMap<String,CommunicationProvider> directPlugins;
    private HashMap<String,StreamableConfiguration> directPluginsConfigs;
    private void prepareDirectDevices() {
        if(directPlugins == null)//ports != null){
            directPlugins = new HashMap<>();
            for (Plugin plugin: PluginRegistry.getInstance().getPluginData().getPluginsFor("mo.communication.CommunicationProvider")) {
                //if(ports.containsKey(plugin.getName())){
                    CommunicationProvider c = (CommunicationProvider) plugin.getNewInstance();
                    if(!directPlugins.containsKey(c.getName()))
                        directPlugins.put(c.getName(),c);
                    //new Thread(()->c.initNewConfiguration(multicastIP, ports.get(plugin.getName()))).start();
                //}
            }
        //}
    }
    
    public void initStreaming(){
        if(!isOnline) return;
        System.out.println("ENTRO A INISTREAMINT CON "+isStreaming+" y "+directPluginsConfigs);
        if(!isStreaming && directPluginsConfigs != null){
            for(String k : directPluginsConfigs.keySet()){
                new Thread(()->{
                    if(directPluginsConfigs.get(k) instanceof ConnectionListener){
                        ServerConnection.getInstance().subscribeListener((ConnectionListener) directPluginsConfigs.get(k));
                        System.out.println("el plugin es listener");
                    }
                    if(directPluginsConfigs.get(k) instanceof ConnectionSender){
                        ((ConnectionSender)directPluginsConfigs.get(k)).subscribeListener(this);
                        System.out.println("el plugin es sender");
                    }
                    ((StreamableConfiguration)directPluginsConfigs.get(k)).startStreaming();
                    
                    System.out.println("transmitiendo "+((StreamableConfiguration)directPluginsConfigs.get(k)).toString());
                }).start();
            }
            isStreaming = true;
            return;
        }
        
        isStreaming = false;
//        
//        if(!isStreaming && getDirectDevs() != null){
//            for(String k : getDirectDevs().keySet()){
//                CommunicationProvider cp = getDirectDevs().get(k);
//                ((StreamableConfiguration)cp.getConfigurations().get(0)).startStreaming();
//            }
//            isStreaming = true;
//        }
    }
    private HashMap<String,Object> devConfig;
    public void configureDirectDevices(HashMap<String,Object> devConf){
        devConfig = devConf;
        System.out.println("ENTRO A CONFIGURAR PLUGINS CON "+devConf);
        if(directPluginsConfigs == null)
            directPluginsConfigs = new HashMap<>();
        int auxPort;
        String auxNameDev;
        for(String k: devConf.keySet()){
            for(String nameDev : getDirectDevs().keySet()){
                if(removeEnd(k).equals(nameDev) && getEnd(k).equals("PORT")){
                    CommunicationProvider cp = getDirectDevs().get(nameDev);
                    auxPort = Integer.parseInt((String) devConf.get(nameDev+" PORT"));
                    if(devConf.containsKey(nameDev+" DEVICE")){
                        auxNameDev = (String) devConf.get(nameDev+" DEVICE");
                        cp.initNewConfiguration(multicastIP, auxPort, auxNameDev);
                    }
                    else{
                        cp.initNewConfiguration(multicastIP, auxPort, null);
                    }
//                    ((StreamableConfiguration)cp.getConfigurations().get(0)).startStreaming();
                    directPluginsConfigs.put(nameDev, ((StreamableConfiguration)cp.getConfigurations().get(0)));
                }
            }
        }
    }
    
    public void sendDirectConfigs(RemoteClient rc){
        ArrayList<RemoteClient> inactiveClients = new ArrayList<>();
        PetitionResponse response = new PetitionResponse(Command.DIRECT_CONFIGS,devConfig);
        boolean sent = false;
        if(rc != null){
            sent = rc.send(response);
            if(!sent)
                inactiveClients.add(rc);
        }
        clients.removeAll(inactiveClients);
    }
    
    
    public HashMap<String,CommunicationProvider> getDirectDevs(){
        prepareDirectDevices();
        return directPlugins;
    }
    
    public String removeEnd(String in) {
        String firstWords = in.substring(0, in.lastIndexOf(" "));
        return firstWords;
    }
    
    public String getEnd(String in) {
        String firstWords = in.substring(in.lastIndexOf(" ")+1);
        return firstWords;
    }

    @Override
    public void subscribeListener(ConnectionListener c) {
        if (listeners == null)
            listeners = new ArrayList<>();
        listeners.add(c);
        if(c instanceof CommunicationConfiguration)
            System.out.println("Se ha suscrito uno más de nombre: "+((CommunicationConfiguration)c).toString());
    }

    @Override
    public void unsubscribeListener(ConnectionListener c) {
        if (listeners == null || listeners.isEmpty() || !listeners.contains(c))
            return;
        listeners.remove(c);
        System.out.println("Se ha suscrito uno más");
    }

    @Override
    public void onMessageReceived(Object obj, PetitionResponse pr) {
        try {
            this.handlerTCP(null, pr);
        } catch (IOException ex) {
            Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void disconnect(RemoteClient rc) {
        try {
            rc.endConnection();
        } catch (IOException ex) {
            Logger.getLogger(ServerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
