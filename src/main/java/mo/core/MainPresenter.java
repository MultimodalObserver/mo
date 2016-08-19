package mo.core;

import mo.filemanagement.Project;
import java.awt.Dimension;
import mo.core.preferences.AppPreferencesWrapper;
import mo.core.preferences.PreferencesManager;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import mo.filemanagement.FileRegistry;

public class MainPresenter {

    private final MainWindow view;

    private final PreferencesManager prefManager = new PreferencesManager();

    private AppPreferencesWrapper preferences;

    public MainPresenter(MainWindow w) {
        view = w;

        Class c = AppPreferencesWrapper.class;
        File prefFile = new File(MultimodalObserver.APP_PREFERENCES_FILE);
        preferences
                = (AppPreferencesWrapper) prefManager.loadOrCreate(c, prefFile);

        view.setLocation(preferences.getFrameX(), preferences.getFrameY());
        view.setPreferredSize(new Dimension(preferences.getFrameWidth(),
                preferences.getFrameHeight()));

        List<String> projectsNotFound = new ArrayList<>();
        preferences.getOpenedProjects().stream().forEach((openedProject) -> {
            File f = new File(openedProject.getLocation());

            if (f.exists()) {
                //addFile(openedProject.getLocation()));
            } else {
                projectsNotFound.add(openedProject.getLocation());
            }

        });

        for (String projectPath : projectsNotFound) {
            preferences.removeOpenedProject(projectPath);
        }
        prefManager.save(preferences, prefFile);
        //view.refreshFiles();

        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                File prefFile = new File(MultimodalObserver.APP_PREFERENCES_FILE);
                preferences = (AppPreferencesWrapper) prefManager.loadOrCreate(c, prefFile);
                preferences.setFrameX(view.getX());
                preferences.setFrameY(view.getY());
                preferences.setFrameWidth(view.getWidth());
                preferences.setFrameHeight(view.getHeight());
                prefManager.save(preferences, prefFile);
            }
        });
    }

    public void start() {
        SwingUtilities.invokeLater(view::createAndShowGUI);
    }

    private void moduleSelected(ActionEvent e) {
//        //System.out.println(e);
//        try {
//            try {
//                main.InstanciateModule(e.getActionCommand().replace("/", "."), view);
//            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                Logger.getLogger(MainPresenter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(MainPresenter.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public AppPreferencesWrapper getPreferences() {
        return preferences;
    }

}
