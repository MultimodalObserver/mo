package mo.core.filemanagement.project;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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

import mo.core.plugin.Plugin;
import java.util.List;
import java.util.ArrayList;
import mo.core.plugin.PluginRegistry;

import mo.exchange.data.importer.plugin.ImportPlugin;
// import mo.exchange.data.importer.plugin.ImportProjectPlugin;
import mo.exchange.data.importer.plugino.ImportProjectPlugin;
import java.awt.event.ActionListener;
// import mo.echange.data.importer.ImportProjectManager;
import mo.echange.data.importer.ImportProjectManager2;

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
    // private List<JMenuItem> items;
    private JMenuItem importProject; // alonso quitar

    private I18n inter;

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

        List<Plugin> plugins = PluginRegistry.getInstance().getPluginsFor("mo.exchange.data.importer.plugino.ImportProjectPlugin");
        List<JMenuItem> items = getJMenuItemsFor(plugins);
        addItemsToMenu(items);
    }

    private void addItemsToMenu(List<JMenuItem> items) {
        for (JMenuItem item : items) {
            projectMenu.add(item);
        }
    }

    private List<JMenuItem> getJMenuItemsFor(List<Plugin> plugins) {
        List<JMenuItem> items = new ArrayList();

        JMenuItem pluginItem;
        ImportProjectPlugin p;
        for (Plugin plugin : plugins) {
            p = (ImportProjectPlugin) plugin.getNewInstance();
            pluginItem = new JMenuItem();
            pluginItem.setName(p.getName());
            pluginItem.setText(p.getName());
            pluginItem.addActionListener(new ActionListener() {
                ImportProjectPlugin p = (ImportProjectPlugin) plugin.getNewInstance();
                @Override
                public void actionPerformed(ActionEvent e) {
                    importProject(p);
                }
            });

            items.add(pluginItem);
        }

        return items;
    }

    private void importProject(ImportProjectPlugin plugin) {
        System.out.println("importProject()");

        // ImportProjectManager manager = new ImportProjectManager(plugin);
        ImportProjectManager2 manager = new ImportProjectManager2(plugin);
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
