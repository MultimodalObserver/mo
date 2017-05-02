package mo.core.plugin;

import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import javax.swing.JMenuItem;
import mo.core.ui.menubar.IMenuBarItemProvider;
import static mo.core.ui.menubar.MenuItemLocations.UNDER;

public class PluginPlainViewer implements IMenuBarItemProvider {
    
    JMenuItem menuLauncher;
    
    public PluginPlainViewer() {
        menuLauncher = new JMenuItem("Plain Plugin Viewer");
        menuLauncher.addActionListener((ActionEvent e) -> {
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
    
    public static void print() {
        List<Plugin> plugins = PluginRegistry.getInstance().getPlugins();
        System.out.println("Plugins");
        for (Plugin plugin : plugins) {
            System.out.println("    "+plugin.getId());
            for (Dependency dependency : plugin.getDependencies()) {
                System.out.println("        "+dependency.getId());
            }
        }
        System.out.println("Extension Points");
        List<ExtPoint> e = PluginRegistry.getInstance().getExtPoints();
        for (ExtPoint extPoint : e) {
            System.out.println("    "+extPoint.getId());
            for (Plugin plugin : extPoint.getPlugins()) {
                System.out.println("        "+plugin.getId());
            }
        }
    }
}
