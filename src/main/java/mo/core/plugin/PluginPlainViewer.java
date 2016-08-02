package mo.core.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import javax.swing.JMenuItem;
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
public class PluginPlainViewer implements IMenuBarItem {
    
    JMenuItem menuLauncher;
    
    public PluginPlainViewer() {
        menuLauncher = new JMenuItem("Plugin Viewer");
        menuLauncher.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("hola "+new Date());
                List<Plugin> plugins = PluginRegistry.getInstance().getPlugins();
                for (Plugin plugin : plugins) {
                    System.out.println(plugin);
                }
            }
        });
    }
    
    @Override
    public JMenuItem getItem() {
        return menuLauncher;
    }

    @Override
    public int getRelativePosition() {
        return IMenuBarItem.UNDER;
    }

    @Override
    public String getRelativeTo() {
        return "plugins";
    }    
}
