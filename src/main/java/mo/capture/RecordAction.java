package mo.capture;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import mo.communication.Command;
import mo.communication.ConnectionListener;
import mo.communication.ConnectionSender;
import mo.communication.PetitionResponse;
import mo.communication.ServerConnection;
import mo.communication.streaming.capture.PluginCaptureSender;
import mo.core.ui.GridBConstraints;
import mo.core.ui.Utils;
import mo.core.ui.dockables.DockablesRegistry;
import mo.organization.*;
import mo.core.I18n;

public class RecordAction implements StageAction, ConnectionListener, ConnectionSender  {

    private I18n i18n;
    private final static String ACTION_NAME = "Record";
    private ProjectOrganization org;
    private Participant participant;
    private List<RecordableConfiguration> configurations;
    private RecordDialog dialog;
    File storageFolder;
    boolean isPaused = false;
    boolean isTrayEnable = false, isRecording = false;

    private static final Image recImage
            = Utils.createImageIcon("images/rec.png", RecordAction.class).getImage();

    private static final Image pausedImage
            = Utils.createImageIcon("images/rec-paused.png", RecordAction.class).getImage();
    
    private static final Logger logger = Logger.getLogger(RecordAction.class.getName());

    public RecordAction() {
        i18n = new I18n(RecordAction.class);
        configurations = new ArrayList<>();
        ServerConnection.getInstance().addListener(this);
        this.subscribeListener(ServerConnection.getInstance());
    }

    @Override
    public String getName() {
        return i18n.s("CaptureStage.record");
    }

    @Override
    public void init(
            ProjectOrganization organization,
            Participant participant,
            StageModule stage) {

        this.org = organization;
        this.participant = participant;

        ArrayList<Configuration> configs = new ArrayList<>();
        for (StagePlugin plugin : stage.getPlugins()) {
            for (Configuration configuration : plugin.getConfigurations()) {
                configs.add(configuration);
            }
        }

        storageFolder = new File(org.getLocation(),
                "participant-" + participant.id + "/"
                + stage.getCodeName().toLowerCase());
        storageFolder.mkdirs();

        dialog = new RecordDialog(configs);
        configurations = dialog.showDialog();
        if (configurations != null) {
            startRecording();
        }
    }

    private void startRecording() {

        try {
            for (RecordableConfiguration config : configurations) {
                config.setupRecording(storageFolder, org, participant);
            }
            isRecording = true;
            JFrame frame = DockablesRegistry.getInstance().getMainFrame();
            if (SystemTray.isSupported()) {
                try {
                    createAndShowTray();
                } catch (AWTException ex) {
                    createAndShowControls();
                }
            } else {
                createAndShowControls();
            }
            frame.setVisible(false);
            ServerConnection.getInstance().setParticipantInfo(org, participant, storageFolder);

            for (RecordableConfiguration config : configurations) {
                if (config instanceof PluginCaptureSender)
                    ServerConnection.getInstance().addActiveCapturePlugin((PluginCaptureSender)config);
                if (config instanceof ConnectionListener)
                    ServerConnection.getInstance().subscribeListener((ConnectionListener) config);
                if (config instanceof ConnectionSender)
                    ((ConnectionSender)config).subscribeListener(ServerConnection.getInstance());
                System.out.println(config.getId());
                config.startRecording();
            }
            
            ServerConnection.getInstance().sendInitialConfigs(null);
            if(listeners != null){
                HashMap<String,Object> map = new HashMap<>();
                map.put("recording_state", "recording");
                PetitionResponse pr = new PetitionResponse(Command.UPDATE_STATE_RECORDING,map);
                for(ConnectionListener listener: listeners){
                    listener.onMessageReceived(this, pr);
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private MenuItem pauseResume, stop, cancel;
    private void createAndShowTray() throws AWTException {

        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(recImage);
        final SystemTray tray = SystemTray.getSystemTray();

        pauseResume = new MenuItem("Pause Recording");

        stop = new MenuItem("Stop Recording");
        cancel = new MenuItem("Cancel Recording");

        popup.add(pauseResume);
        popup.add(stop);
        popup.add(cancel);

        trayIcon.setPopupMenu(popup);

        pauseResume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    resumeRecording();
                    trayIcon.setImage(recImage);
                    pauseResume.setLabel("Pause Recording");
                } else {
                    pauseRecording();
                    trayIcon.setImage(pausedImage);
                    pauseResume.setLabel("Resume Recording");
                }
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelRecording();
                tray.remove(trayIcon);
                pauseResume =null; stop=null; cancel = null;
            }
        });

        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopRecording();
                tray.remove(trayIcon);
                pauseResume =null; stop=null; cancel = null;
            }
        });

        isTrayEnable = true;
        tray.add(trayIcon);
    }

    private JButton stopButton, pauseButton, cancelButton;
    private void createAndShowControls() {
        JDialog controlsDialog
                = new JDialog((JFrame) null, "Recording Controls");

        controlsDialog.setIconImage(recImage);
        controlsDialog.setLayout(new GridBagLayout());

        GridBConstraints gbc = new GridBConstraints();
        gbc.f(GridBConstraints.HORIZONTAL);
        gbc.i(new Insets(5, 5, 5, 5));

        stopButton = new JButton("Stop");
        pauseButton = new JButton("||  Pause");
        cancelButton = new JButton("Cancel");

        controlsDialog.add(pauseButton, gbc);
        controlsDialog.add(stopButton, gbc.gx(1));
        controlsDialog.add(cancelButton, gbc.gx(2));

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    resumeRecording();
                    pauseButton.setText("|| Pause");
                    controlsDialog.setIconImage(recImage);
                } else {
                    pauseRecording();
                    pauseButton.setText("> Resume");
                    controlsDialog.setIconImage(pausedImage);
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopRecording();
                controlsDialog.dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelRecording();
                controlsDialog.dispose();
            }
        });

        controlsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelRecording();
                controlsDialog.dispose();
            }
        });
        controlsDialog.pack();
        controlsDialog.setVisible(true);
        isTrayEnable = false;
    }

    private void cancelRecording() {
        for (RecordableConfiguration configuration : configurations) {
            configuration.cancelRecording();
        }
        JFrame frame = DockablesRegistry.getInstance().getMainFrame();
        frame.setVisible(true);
        isRecording = false;
        if(listeners != null){
            HashMap<String,Object> map = new HashMap<>();
            map.put("recording_state", "cancelled");
            PetitionResponse pr = new PetitionResponse(Command.UPDATE_STATE_RECORDING,map);
            for(ConnectionListener listener: listeners){
                listener.onMessageReceived(this, pr);
            }
        }
    }

    private void stopRecording() {
        for (RecordableConfiguration configuration : configurations) {
            configuration.stopRecording();
        }
        JFrame frame = DockablesRegistry.getInstance().getMainFrame();
        frame.setVisible(true);
        isRecording = false;
        if(listeners != null){
            HashMap<String,Object> map = new HashMap<>();
            map.put("recording_state", "stopped");
            PetitionResponse pr = new PetitionResponse(Command.UPDATE_STATE_RECORDING,map);
            for(ConnectionListener listener: listeners){
                listener.onMessageReceived(this, pr);
            }
        }
    }

    private void pauseRecording() {
        for (RecordableConfiguration configuration : configurations) {
            configuration.pauseRecording();
        }
        isPaused = true;
        if(listeners != null){
            HashMap<String,Object> map = new HashMap<>();
            map.put("recording_state", "paused");
            PetitionResponse pr = new PetitionResponse(Command.UPDATE_STATE_RECORDING,map);
            for(ConnectionListener listener: listeners){
                listener.onMessageReceived(this, pr);
            }
        }
    }

    private void resumeRecording() {
        for (RecordableConfiguration configuration : configurations) {
            configuration.resumeRecording();
        }
        isPaused = false;
        if(listeners != null){
            HashMap<String,Object> map = new HashMap<>();
            map.put("recording_state", "recording");
            PetitionResponse pr = new PetitionResponse(Command.UPDATE_STATE_RECORDING,map);
            for(ConnectionListener listener: listeners){
                listener.onMessageReceived(this, pr);
            }
        }
    }

    @Override
    public void onMessageReceived(Object obj, PetitionResponse pr) {
        //System.out.println("Record action recibe 1: "+connection.mensajeRecibido+"\n");
        if(pr != null && isRecording){
            //System.out.println("Record action recibe 2: "+connection.mensajeRecibido+"\n");
            try{
                switch (pr.getType()) {
                    case Command.STOP_RECORDING:
                        if(isTrayEnable)
                            stop.getActionListeners()[0].actionPerformed(
                                        new ActionEvent(stop,ActionEvent.ACTION_PERFORMED,stop.getActionCommand()));
                        else stopButton.doClick();
                        break;
                        
                    case Command.PAUSE_RESUME_RECORDING:
                        if(isPaused){
                            if(isTrayEnable)
                            pauseResume.getActionListeners()[0].actionPerformed(
                                    new ActionEvent(pauseResume,ActionEvent.ACTION_PERFORMED,pauseResume.getActionCommand()));
                            else pauseButton.doClick();
                        }
                        else{
                            if(isTrayEnable)
                                pauseResume.getActionListeners()[0].actionPerformed(
                                        new ActionEvent(pauseResume,ActionEvent.ACTION_PERFORMED,pauseResume.getActionCommand()));
                            else pauseButton.doClick();
                        }
                        break;
                        
                    case Command.CANCEL_RECORDING:
                        if(isTrayEnable)
                            cancel.getActionListeners()[0].actionPerformed(
                                    new ActionEvent(cancel,ActionEvent.ACTION_PERFORMED,cancel.getActionCommand()));
                        else cancelButton.doClick();
                        break;
                    default:
                        break;
                }
            }catch(NullPointerException e){
                System.out.println("Ha ocurrido un error");
            }
        }
    }

    ArrayList<ConnectionListener> listeners;
    @Override
    public void subscribeListener(ConnectionListener c) {
        if(listeners == null) listeners = new ArrayList<>();
        listeners.add(c);
    }

    @Override
    public void unsubscribeListener(ConnectionListener c) {
        if(listeners == null) listeners = new ArrayList<>();
        if(listeners.contains(c)) listeners.remove(c);
    }
}
