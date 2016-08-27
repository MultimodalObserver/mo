package mo.organization.tree;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JMenuItem;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.ui.dockables.DockablesRegistry;
import mo.filemanagement.FilePopupMenu;
import mo.filemanagement.project.ProjectOptionProvider;

@Extension(
    xtends = {
        @Extends(
                extensionPointId = "mo.filemanagement.project.ProjectOptionProvider"
        )
    }
)
public class ProjectOrganizationPlugin implements ProjectOptionProvider {

    JMenuItem addOrgItem;

    public ProjectOrganizationPlugin() {
        addOrgItem = new JMenuItem("add organization");
        addOrgItem.addActionListener((ActionEvent e) -> {
            addProjectClicked(e);
        });
    }
    
    private void addProjectClicked(ActionEvent event) {
        JMenuItem clickedItem = (JMenuItem) event.getSource();
        FilePopupMenu popup = (FilePopupMenu) clickedItem.getParent();
        File projectFolder = popup.getFile();
        
        OrganizationDockable dock = new OrganizationDockable();
        dock.setTitleText(projectFolder.getName()+" - Organization");
        dock.setProjectPath(projectFolder.getAbsolutePath());
        
        DockablesRegistry.getInstance()
                .addDockableInProjectGroup(projectFolder.getAbsolutePath(), dock);
    }

    @Override
    public JMenuItem getOption() {
        return addOrgItem;
    }
    
}
