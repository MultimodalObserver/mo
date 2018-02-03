package mo.exchange.data.importer.plugin;

import mo.organization.Participant;
import mo.core.plugin.ExtensionPoint;
import java.util.List;
import java.io.File;

@ExtensionPoint
public interface ImportDataPlugin extends ImportPlugin
{
	boolean isValidOrigin(File carpeta);
	List<String> getCompatibleCreators();
	List<Participant> getParticipants();
}