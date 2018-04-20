package mo.core.filemanagement.project;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import mo.core.I18n;
import mo.core.MultimodalObserver;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.preferences.AppPreferencesWrapper;
import mo.core.preferences.PreferencesManager;
import mo.core.ui.WizardDialog;
import mo.core.ui.menubar.IMenuBarItemProvider;
import mo.core.filemanagement.FileRegistry;
import static mo.core.ui.menubar.MenuItemLocations.UNDER;

@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.core.ui.menubar.IMenuBarItemProvider"
            )
        }
)
public class ProjectManagement implements IMenuBarItemProvider {

    private JMenu projectMenu;
    private JMenuItem newProject, openProject, closeProject;
    private I18n inter;
    private final String dockablesProjectFileName = "dockables.xml";

    public ProjectManagement() {
        inter = new I18n(ProjectManagement.class);
        
        projectMenu = new JMenu();
        projectMenu.setName("project");
        projectMenu.setText(inter.s("ProjectManagement.projectMenu"));
        
        newProject = new JMenuItem();
        newProject.setName("new project...");
        newProject.setText(inter.s("ProjectManagement.newProjectMenuItem"));
        
        openProject = new JMenuItem();
        openProject.setName("open project...");
        openProject.setText(inter.s("ProjectManagement.openProjectMenuItem"));

        newProject.addActionListener(this::newProject);

        openProject.addActionListener((ActionEvent e) -> {
            openProject();
        });

        projectMenu.add(newProject);
        projectMenu.add(openProject);
    }

    private void openProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || ProjectUtils.isProjectFolder(file);
            }

            @Override
            public String getDescription() {
                return inter.s("ProjectManagement.fileFilterDescription");
            }
        });

        int returnValue = chooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            //System.out.println(chooser.getSelectedFile());
            File selected = chooser.getSelectedFile();
            if (ProjectUtils.isProjectFolder(selected)) {
                
                //fix bug 
                File dockablesFile = new File(selected,dockablesProjectFileName);
                updateDockablesProjectFile(dockablesFile);
                //fix bug
                
                Project project = new Project(selected.getAbsolutePath());
                //saveProjectInAppPreferences(project);
                FileRegistry.getInstance().addOpenedProject(project);
            }
        }
    }
    
    private void updateDockablesProjectFile(File dockablesFile){
    
               try {
                    //correccion de bug
                    XElement root = XIO.readUTF(new FileInputStream(dockablesFile));                
                    for(XElement dockable :  root.getElements("dockable")){
                        XElement originaGroup = dockable.getElement("group");
                        String originalGroupPath = originaGroup.getString();
                        if(originalGroupPath!=null){
                            File originalDir =  new File(originalGroupPath);
                            if(!originalDir.exists()){
                                originaGroup.setString(dockablesFile.getParentFile().getAbsolutePath());
                            }
                        }
                    }
                     XIO.writeUTF(root, new FileOutputStream(dockablesFile));
                } catch (IOException ex) {
                    Logger.getLogger(ProjectManagement.class.getName()).log(Level.SEVERE, null, ex);
                }    
    }

    private void saveProjectInAppPreferences(Project project) {
        PreferencesManager pm = new PreferencesManager();
        AppPreferencesWrapper app = (AppPreferencesWrapper) pm.loadOrCreate(AppPreferencesWrapper.class, new File(MultimodalObserver.APP_PREFERENCES_FILE));
        app.addOpenedProject(project.getFolder().getAbsolutePath());
        pm.save(app, new File(MultimodalObserver.APP_PREFERENCES_FILE));
        //System.out.println(app);
    }

    private void newProject(ActionEvent e) {
        WizardDialog w = new WizardDialog(
                null, inter.s("NewProjectWizardPanel.newProjectWizardTitle"));
        w.addPanel(new NewProjectWizardPanel(w));
        HashMap<String, Object> result = w.showWizard();
        if (result != null) {

            File project = new File((String) result.get("projectFolder"));
            if (project.mkdir()) {
                Project p = new Project((String) result.get("projectFolder"));

                saveProjectInAppPreferences(p);

                FileRegistry.getInstance().addOpenedProject(p);

            }
        }
    }

    @Override
    public JMenuItem getItem() {
        return projectMenu;
    }

    @Override
    public int getRelativePosition() {
        return UNDER;
    }

    @Override
    public String getRelativeTo() {
        return "file";
    }

}
