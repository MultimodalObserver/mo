package mo.filemanagement.project;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import mo.core.MultimodalObserver;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.preferences.AppPreferencesWrapper;
import mo.core.preferences.PreferencesManager;
import mo.core.ui.WizardDialog;
import mo.core.ui.menubar.IMenuBarItemProvider;
import mo.filemanagement.FileRegistry;

@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.core.ui.menubar.IMenuBarItemProvider"
            )
        }
)
public class ProjectManagement implements IMenuBarItemProvider {

    JMenu projectMenu;
    JMenuItem newProject, openProject;

    public ProjectManagement() {
        projectMenu = new JMenu("Project");
        newProject = new JMenuItem("New Project...");
        openProject = new JMenuItem("Open Project...");

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
                return "Folders and Multimodal Observer Project Folders";
            }
        });

        int returnValue = chooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            //System.out.println(chooser.getSelectedFile());
            File selected = chooser.getSelectedFile();
            if (ProjectUtils.isProjectFolder(selected)) {
                Project project = new Project(selected.getAbsolutePath());
                //saveProjectInAppPreferences(project);
                FileRegistry.getInstance().addOpenedProject(project);

            }
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
        WizardDialog w = new WizardDialog(null, "New Project");
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
        return IMenuBarItemProvider.UNDER;
    }

    @Override
    public String getRelativeTo() {
        return "file";
    }

}
