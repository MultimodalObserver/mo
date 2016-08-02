package mo.core.ui.menubar;

import java.util.List;
import mo.core.plugin.ExtensionPoint;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
@ExtensionPoint
public interface IMenuBarItems {
    public List<MenuBarItem> getItems();
}
