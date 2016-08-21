package mo.core.ui.dockables;

import bibliothek.util.xml.XElement;
import java.io.File;

public interface StorableDockable {
    String toFileContent();
    DockableElement dockableFromFile(String fileContent);
}
