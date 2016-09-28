package mo.organization.visualization.tree;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.ui.dockables.DockablesRegistry;
import mo.filemanagement.FilePopupMenu;
import mo.filemanagement.project.ProjectOptionProvider;
import mo.organization.OrganizationVisualizationMenuItemProvider;
import mo.organization.ProjectOrganization;

@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.organization.OrganizationVisualizationMenuItemProvider"
            )
        }
)
public class ProjectOrganizationPlugin implements OrganizationVisualizationMenuItemProvider {

    JMenuItem addOrgItem;

    public ProjectOrganizationPlugin() {
        addOrgItem = new JMenuItem("Default Tree");
        addOrgItem.addActionListener((ActionEvent e) -> {
            addProjectClicked(e);
        });
    }

    private void addProjectClicked(ActionEvent event) {

        JComponent c = (JComponent) event.getSource();
        System.out.println("comp " + c);

        System.out.println(c.getClientProperty("file"));

        File projectFolder = (File) c.getClientProperty("file");

        if (projectFolder != null) {

            File treeOrgFile = new File(projectFolder, "organization-visualization-tree.xml");

            if (!treeOrgFile.exists()) {
                
                ProjectOrganization o = new ProjectOrganization(projectFolder.getAbsolutePath());

                OrganizationDockable dock = new OrganizationDockable(o);
                dock.setTitleText(projectFolder.getName() + " - Organization");
                dock.setProjectPath(projectFolder.getAbsolutePath());

                DockablesRegistry.getInstance()
                        .addDockableInProjectGroup(projectFolder.getAbsolutePath(), dock);

            }
        }
    }

    @Override
    public JMenuItem getMenuItem() {
        return addOrgItem;
    }
}
