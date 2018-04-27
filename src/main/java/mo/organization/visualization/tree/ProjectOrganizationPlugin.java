package mo.organization.visualization.tree;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import mo.core.I18n;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
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

    private final JMenuItem addOrgItem;
    private I18n i18n;
    
    public ProjectOrganizationPlugin() {
        addOrgItem = new JMenuItem("Default Tree");
        addOrgItem.addActionListener((ActionEvent e) -> {
            addProjectClicked(e);
        });
        
        i18n = new I18n(ProjectOrganizationPlugin.class);
    }

    private void addProjectClicked(ActionEvent event) {

        JComponent c = (JComponent) event.getSource();

        File projectFolder = (File) c.getClientProperty("file");

        if (projectFolder != null) {

            File treeOrgFile = new File(projectFolder, "organization-visualization-tree.xml");

            if (!treeOrgFile.exists()) {
                
                ProjectOrganization o = new ProjectOrganization(projectFolder.getAbsolutePath());

                OrganizationDockable dock = new OrganizationDockable(o);
                dock.setTitleText(projectFolder.getName() + i18n.s("ProjectOrganizationPlugin.titleSufix"));
                dock.setProjectPath(projectFolder.getAbsolutePath());

                DockablesRegistry.getInstance()
                        .addDockableInProjectGroup(projectFolder.getAbsolutePath(), dock);

            }
            
            else{
                
                try {
                    XElement root = XIO.readUTF(new FileInputStream(treeOrgFile));
                    String projectName = root.getElement("name").getString();
                    System.out.println(projectName+i18n.s("ProjectOrganizationPlugin.titleSufix"));
                    DockableElement d = DockablesRegistry.getInstance().getDockableByTitle(projectName+i18n.s("ProjectOrganizationPlugin.titleSufix"));
                    if(d!=null){
                        d.setVisible(true);
                        DockablesRegistry.DockableCheckBoxMenuItem menuItem = DockablesRegistry.getInstance().getDockableMeuItemByTitle(projectName+i18n.s("ProjectOrganizationPlugin.titleSufix"));
                        if(menuItem!=null){menuItem.setSelected(true);}
                    }
                    
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProjectOrganizationPlugin.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ProjectOrganizationPlugin.class.getName()).log(Level.SEVERE, null, ex);
                }

            
            
            }
        }
    }

    @Override
    public JMenuItem getMenuItem() {
        return addOrgItem;
    }
}
