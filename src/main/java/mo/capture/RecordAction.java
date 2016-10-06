package mo.capture;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import mo.core.ui.GridBConstraints;
import mo.core.ui.Utils;
import mo.core.ui.dockables.DockablesRegistry;
import mo.organization.*;

public class RecordAction implements StageAction {

    private final static String ACTION_NAME = "Record";
    private ProjectOrganization org;
    private Participant participant;
    private List<RecordableConfiguration> configurations;
    private RecordDialog dialog;
    File storageFolder;
    boolean isPaused = false;

    public RecordAction() {
        configurations = new ArrayList<>();
    }

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public void init(
            ProjectOrganization organization,
            Participant participant,
            Stage stage) {

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
                + stage.getName().toLowerCase());
        storageFolder.mkdirs();

        dialog = new RecordDialog(configs);
        configurations = dialog.showDialog();
        if (configurations != null) {
            startRecording();
        }
    }

    private void startRecording() {
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

        for (RecordableConfiguration config : configurations) {
            config.setupRecording(storageFolder, org, participant);
        }

        for (RecordableConfiguration config : configurations) {
            config.startRecording();
        }
    }

    private void createAndShowTray() throws AWTException {

        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon
                = new TrayIcon(Utils.createImageIcon("rec.png",
                        RecordAction.class).getImage());
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem pauseResume = new MenuItem("Pause Recording");

        MenuItem stop = new MenuItem("Stop Recording");
        MenuItem cancel = new MenuItem("Cancel Recording");

        popup.add(pauseResume);
        popup.add(stop);
        popup.add(cancel);

        trayIcon.setPopupMenu(popup);

        pauseResume.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    resumeRecording();
                    trayIcon.setImage(Utils.createImageIcon("rec.png",
                            RecordAction.class).getImage());
                    pauseResume.setLabel("Pause Recording");
                } else {
                    pauseRecording();
                    trayIcon.setImage(Utils.createImageIcon("rec-paused.png",
                            RecordAction.class).getImage());
                    pauseResume.setLabel("Resume Recording");
                }
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelRecording();
                tray.remove(trayIcon);
            }
        });

        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopRecording();
                tray.remove(trayIcon);
            }
        });

        tray.add(trayIcon);
    }

    private void createAndShowControls() {
        JDialog controlsDialog
                = new JDialog((JFrame) null, "Recording Controls");

        controlsDialog.setIconImage(Utils.createImageIcon("rec.png",
                RecordAction.class).getImage());
        controlsDialog.setLayout(new GridBagLayout());

        GridBConstraints gbc = new GridBConstraints();
        gbc.f(GridBConstraints.HORIZONTAL);
        gbc.i(new Insets(5, 5, 5, 5));

        JButton stopButton = new JButton("Stop");
        JButton pauseButton = new JButton("||  Pause");
        JButton cancelButton = new JButton("Cancel");

        controlsDialog.add(pauseButton, gbc);
        controlsDialog.add(stopButton, gbc.gx(1));
        controlsDialog.add(cancelButton, gbc.gx(2));

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    resumeRecording();
                    pauseButton.setText("|| Pause");
                    controlsDialog.setIconImage(Utils.createImageIcon("rec.png",
                            RecordAction.class).getImage());
                } else {
                    pauseRecording();
                    pauseButton.setText("> Resume");
                    controlsDialog.setIconImage(Utils.createImageIcon("rec-paused.png",
                            RecordAction.class).getImage());
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

    }

    private void cancelRecording() {
        for (RecordableConfiguration configuration : configurations) {
            configuration.cancelRecording();
        }
        JFrame frame = DockablesRegistry.getInstance().getMainFrame();
        frame.setVisible(true);

    }

    private void stopRecording() {
        for (RecordableConfiguration configuration : configurations) {
            configuration.stopRecording();
        }
        JFrame frame = DockablesRegistry.getInstance().getMainFrame();
        frame.setVisible(true);
    }

    private void pauseRecording() {
        for (RecordableConfiguration configuration : configurations) {
            configuration.pauseRecording();
        }
        isPaused = true;
    }

    private void resumeRecording() {
        for (RecordableConfiguration configuration : configurations) {
            configuration.resumeRecording();
        }
        isPaused = false;
    }
}
