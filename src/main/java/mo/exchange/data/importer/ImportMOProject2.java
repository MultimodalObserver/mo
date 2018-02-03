package mo.exchange.data.importer;

import mo.core.plugin.Extension;
import mo.core.plugin.Extends;
import mo.exchange.data.importer.plugino.ImportProjectPlugin;
import mo.exchange.data.ExchangeableData;
import mo.exchange.data.IncompatibleOriginDataException;
import java.io.File;
import java.util.List;
import java.util.IllegalFormatException;


import mo.organization.StageModule;
import mo.capture.CaptureStage;

import mo.exchange.data.ExchangeableProjectData;


@Extension(
    xtends = {
        @Extends(
                extensionPointId = "mo.exchange.data.importer.plugino.ImportProjectPlugin"
        )
    }
)
public class ImportMOProject2 implements ImportProjectPlugin
{
	private File origin;
	private File destiny;
	// private List<ExchangeableData> data;
	private List<ExchangeableProjectData> data;

	public String getCodeName() {
		return "IMPORTPROJECT";
	}

	public String getName() {
		return "Import Project";
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

	public void setImportdata(List<ExchangeableProjectData> data) {
		this.data = data;
	}

	public void execute() {
		System.out.println("i am exchange");

		try {
			Thread.sleep(3000);
		} catch(Exception ignore) {

		}
	}

	public void cancel() {
		System.out.println("i am cancelExchange");
	}
}