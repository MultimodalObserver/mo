package mo.exchange.data.importer;

import mo.core.plugin.Extension;
import mo.core.plugin.Extends;
import mo.exchange.data.importer.plugin.ImportProjectPlugin;
import mo.exchange.data.ExchangeableData;
import mo.exchange.data.IncompatibleOriginDataException;
import java.io.File;
import java.util.List;
import java.util.IllegalFormatException;


import mo.organization.StageModule;
import mo.capture.CaptureStage;

@Extension(
    xtends = {
        @Extends(
                extensionPointId = "mo.exchange.data.importer.plugin.ImportProjectPlugin"
        )
    }
)
public class ImportMOProject implements ImportProjectPlugin
{
	private File origin;
	private File destiny;
	private List<ExchangeableData> data;

	public String getCodeName() {
		return "IMPORTPROJECT";
	}

	public String getName() {
		return "Import MO Project";
	}

	public String getDescription() {
		return "A plugin for import MO projects";
	}

	public void setOrigin(File origin) {
		this.origin = origin;
	}

	public void setDestiny(File destiny) {
		this.destiny = destiny;
	}

	public void setImportdata(List<ExchangeableData> data) {
		this.data = data;
	}

	public void execute() throws IllegalFormatException {
		System.out.println("export bitch");
	}

	public void cancel() {
		System.out.println("cancelExport");
	}

	public StageModule getStage() {
		return new CaptureStage();
	}
}