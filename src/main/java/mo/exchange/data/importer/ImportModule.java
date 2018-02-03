package mo.exchange.data.importer;

import mo.exchange.data.importer.plugin.ImportPlugin;
import mo.exchange.data.ExchangeableData;
import java.util.List;
import java.io.File;

public class ImportModule
{
	private List<ImportPlugin> plugins;

	public ImportModule(List<ImportPlugin> plugins) {
		this.plugins = plugins;
	}

	public void setOrigin(File origin) {
		for (ImportPlugin plugin : plugins) {
			plugin.setOrigin(origin);
		}
	}

	public void setDestiny(File destiny) {
		for (ImportPlugin plugin : plugins) {
			plugin.setDestiny(destiny);
		}
	}

	public void setImportdata(List<ExchangeableData> data) {
		for (ImportPlugin plugin : plugins) {
			plugin.setImportdata(data);
		}
	}

	public void export() {
		// for (ImportPlugin plugin : plugins) {
		// 	plugin.export();
		// }
	}

	public void cancelExport() {
		// for (ImportPlugin plugin : plugins) {
		// 	plugin.cancelExport();
		// }
	}
}