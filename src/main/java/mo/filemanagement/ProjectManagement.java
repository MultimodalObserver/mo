package mo.filemanagement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import mo.core.MultimodalObserver;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.preferences.AppPreferencesWrapper;
import mo.core.preferences.PreferencesManager;
import mo.core.ui.WizardDialog;
import mo.core.ui.menubar.IMenuBarItem;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
@Extension(
       xtends = {
           @Extends(
                   extensionPointId = "mo.core.ui.menubar.IMenuBarItem"
           )
       } 
)
public class ProjectManagement implements IMenuBarItem {
    
    JMenu projectMenu;
    JMenuItem newProjectMenuItem;
    
    public ProjectManagement() {
        projectMenu = new JMenu("Project");
        newProjectMenuItem = new JMenuItem("New Project...");
        
        newProjectMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newProject(e);
            }
        });
        
        projectMenu.add(newProjectMenuItem);
    }

    @Override
    public JMenuItem getItem() {
        return projectMenu;
    }

    @Override
    public int getRelativePosition() {
        return IMenuBarItem.UNDER;
    }

    @Override
    public String getRelativeTo() {
        return "file";
    }
    
    private void newProject(ActionEvent e) {
        WizardDialog w = new WizardDialog(null, "New Project");
        w.addPanel(new NewProjectWizardPanel(w));
        HashMap<String,Object> result = w.showWizard();
        if (result != null){
            System.out.println("mira"+result.get("projectFolder"));
            File project = new File((String) result.get("projectFolder"));
            if (project.mkdir()){
                Project p = new Project((String) result.get("projectFolder"));
                //main.addOpenedProject(p);
                
                PreferencesManager pm = new PreferencesManager();
                AppPreferencesWrapper app = (AppPreferencesWrapper) pm.loadOrCreate(AppPreferencesWrapper.class, new File(MultimodalObserver.APP_PREFERENCES_FILE));
                app.addOpenedProject(p.getFolder().getAbsolutePath());
                pm.save(app, new File(MultimodalObserver.APP_PREFERENCES_FILE));
                
                //view.addOpenedProject(p);
                //view.refreshFiles();
                FileRegistry.getInstance().addOpenedProject(p);
                System.out.println("fin!");
            }
        }
    }
    
}
