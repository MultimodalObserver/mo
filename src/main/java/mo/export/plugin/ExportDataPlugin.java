package mo.export.plugin;

import mo.core.plugin.ExtensionPoint;
import java.util.List;

@ExtensionPoint
public interface ExportDataPlugin extends ExportPlugin {
	List<String> getCompatibleCreators();
}