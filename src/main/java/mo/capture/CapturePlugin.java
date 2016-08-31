package mo.capture;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.organization.StageNode;
import mo.organization.StageNodeProvider;

@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.organization.StageNodeProvider"
            )
        }
)
public class CapturePlugin implements StageNodeProvider {

    StageNode node;
    JMenuItem item;

    public CapturePlugin() {
        node = new CaptureNode();
        item = new JMenuItem("Capture");
        
        for (Plugin plugin : PluginRegistry.getInstance().getPluginsFor("mo.capture.CaptureProvider")) {
            CaptureProvider c = (CaptureProvider) plugin.getNewInstance();
            item.add(c.getMenu());
        }
    }

    @Override
    public StageNode getStageNode() {
        return node;
    }

    @Override
    public JMenuItem getMenuItem() {
        return item;
    }
}
