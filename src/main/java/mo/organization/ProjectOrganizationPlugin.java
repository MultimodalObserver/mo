package mo.organization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JMenuItem;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.ui.dockables.DockablesRegistry;
import mo.filemanagement.FilePopupMenu;
import mo.filemanagement.ProjectOptionProvider;

@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.filemanagement.ProjectOptionProvider"
            )
        }
)
public class ProjectOrganizationPlugin implements ProjectOptionProvider {

    JMenuItem addProjectOrganization;

    public ProjectOrganizationPlugin() {
        addProjectOrganization = new JMenuItem("add organization");
        addProjectOrganization.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addProjectClicked(e);
            }
        });
    }
    
    private void addProjectClicked(ActionEvent event) {
        System.out.println(event);
        System.out.println(((JMenuItem)event.getSource()).getParent());
        JMenuItem clickedItem = (JMenuItem) event.getSource();
        FilePopupMenu popup = (FilePopupMenu) clickedItem.getParent();
        File projectFolder = popup.getFile();
        System.out.println(projectFolder);
        DockablesRegistry.getInstance().addDockableInProjectGroup(projectFolder.getAbsolutePath(), new OrganizationDockable(projectFolder.getName()));
    }

    @Override
    public JMenuItem getOption() {
        return addProjectOrganization;
    }
    
}
