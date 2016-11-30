package mo.core.plugin;

import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import javax.swing.JMenuItem;
import mo.core.ui.menubar.IMenuBarItemProvider;
import static mo.core.ui.menubar.MenuItemLocations.UNDER;


@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.core.ui.menubar.IMenuBarItemProvider"
            )
        }
)
public class PluginPlainViewer implements IMenuBarItemProvider {
    
    JMenuItem menuLauncher;
    
    public PluginPlainViewer() {
        menuLauncher = new JMenuItem("Plain Plugin Viewer");
        menuLauncher.addActionListener((ActionEvent e) -> {
            System.out.println("hola "+new Date());
            List<Plugin> plugins = PluginRegistry.getInstance().getPlugins();
            for (Plugin plugin : plugins) {
                System.out.println(plugin);
            }
        });
    }
    
    @Override
    public JMenuItem getItem() {
        return menuLauncher;
    }

    @Override
    public int getRelativePosition() {
        return UNDER;
    }

    @Override
    public String getRelativeTo() {
        return "plugins";
    }    
}
