package mo.exchange.data.importer.plugino;

import java.io.File;
import java.util.List;
import mo.exchange.data.plugin.ExchangeableDataPlugin;
import mo.organization.Participant;
import mo.core.plugin.ExtensionPoint;

@ExtensionPoint
public interface ImportDataPlugin extends ExchangeableDataPlugin
{
	boolean isValidOrigin(File carpeta);
	List<Participant> getParticipants();
}
