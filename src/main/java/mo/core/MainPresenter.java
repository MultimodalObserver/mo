package mo.core;

import mo.filemanagement.Project;
import java.awt.Dimension;
import mo.core.utils.Utils;
import mo.filemanagement.NewProjectWizardPanel;
import mo.core.ui.WizardDialog;
import mo.core.preferences.AppPreferencesWrapper;
import mo.core.preferences.PreferencesManager;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingUtilities;
import mo.filemanagement.FileRegistry;

public class MainPresenter {
    
    
    
    private final MainWindow view;

    private final PreferencesManager prefManager = new PreferencesManager();
    
    private AppPreferencesWrapper preferences;
    
    public MainPresenter(MainWindow w){
        view = w;
        
        Class c = AppPreferencesWrapper.class;
        File prefFile = new File(MultimodalObserver.APP_PREFERENCES_FILE); 
        preferences =
                (AppPreferencesWrapper) prefManager.loadOrCreate(c, prefFile);
 
        view.setLocation(preferences.getFrameX(), preferences.getFrameY()); 
        view.setPreferredSize(new Dimension(preferences.getFrameWidth(),
                        preferences.getFrameHeight()));
        
        preferences.getOpenedProjects().stream().forEach((openedProject) -> {
            File f = new File(openedProject.getLocation());
            List<Project> toDelete = new ArrayList<>();
            if (f.exists()) {
                FileRegistry.getInstance().addOpenedProject(new Project(openedProject.getLocation()));
            } else {
                
            }
        });
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
        
        //view.setAvaibleModules(main.getModulesClassesList());

        SwingUtilities.invokeLater(view::createAndShowGUI);
        
        view.addNewProjectListener((ActionEvent e) -> {
            newProject(e);
        });
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

    private void newProject(ActionEvent e) {
        WizardDialog w = new WizardDialog(view, "New Project");
        w.addPanel(new NewProjectWizardPanel(w));
        HashMap<String,Object> result = w.showWizard();
        if (result != null){
            //System.out.println("mira"+result.get("projectFolder"));
            File project = new File((String) result.get("projectFolder"));
            if (project.mkdir()){
                Project p = new Project((String) result.get("projectFolder"));
                //main.addOpenedProject(p);
                preferences.addOpenedProject(p.getFolder().getAbsolutePath());
                prefManager.save(preferences, new File(MultimodalObserver.APP_PREFERENCES_FILE));
                
                FileRegistry.getInstance().addOpenedProject(p);
                //view.refreshFiles();
            }
        }
    }

    public AppPreferencesWrapper getPreferences() {
        return preferences;
    }
    
    
}
