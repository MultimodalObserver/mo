package mo.exchange.data.importer.plugin;

import mo.exchange.data.IncompatibleOriginDataException;
import mo.exchange.data.ExchangeableData;
import java.util.List;
import java.util.IllegalFormatException;
import java.io.File;

public interface ImportPlugin
{
	String getCodeName();
	String getName();
	String getDescription();

	void setOrigin(File origin);
	void setDestiny(File destiny);
	void setImportdata(List<ExchangeableData> data);

	void execute() throws IllegalFormatException;
	void cancel();
}