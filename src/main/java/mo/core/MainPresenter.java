package mo.core;

import mo.core.ui.NewProjectWizardPanel;
import mo.core.ui.WizardDialog;
import mo.core.preferences.AppPreferencesWrapper;
import mo.core.preferences.PreferencesManager;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import javax.swing.SwingUtilities;

public class MainPresenter {
    
    private static final String APP_PREFERENCES_FILE = 
            Utils.getBaseFolder()+"/preferences.xml";
    
    private final MainWindow view;

    private final PreferencesManager prefManager = new PreferencesManager();
    
    private final AppPreferencesWrapper preferences;
    
    public MainPresenter(MainWindow w){
        view = w;
        
        Class c = AppPreferencesWrapper.class;
        File prefFile = new File(APP_PREFERENCES_FILE); 
        preferences =
                (AppPreferencesWrapper) prefManager.loadOrCreate(c, prefFile);
 
        preferences.getOpenedProjects().stream().forEach((openedProject) -> {
            File f = new File(openedProject.getLocation());
            if (f.exists())
                view.addOpenedProject(new Project(openedProject.getLocation()));
        });
        view.refreshFiles();
    }

    public void start() {
        
        //view.setAvaibleModules(main.getModulesClassesList());

        SwingUtilities.invokeLater(view::createAndShowGUI);
        
        view.addModulesMenuItemsActionListener((ActionEvent e) -> {
            moduleSelected(e);
        });
        
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
            System.out.println("mira"+result.get("projectFolder"));
            File project = new File((String) result.get("projectFolder"));
            if (project.mkdir()){
                Project p = new Project((String) result.get("projectFolder"));
                //main.addOpenedProject(p);
                preferences.addOpenedProject(p.getFolder().getAbsolutePath());
                prefManager.save(preferences, new File(APP_PREFERENCES_FILE));
                
                view.addOpenedProject(p);
                view.refreshFiles();
            }
        }
    }
}
