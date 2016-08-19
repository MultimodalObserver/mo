package mo.filemanagement;

import javax.swing.JMenuItem;
import mo.core.plugin.ExtensionPoint;

@ExtensionPoint
public interface ProjectOptionProvider {
    JMenuItem getOption();
}
