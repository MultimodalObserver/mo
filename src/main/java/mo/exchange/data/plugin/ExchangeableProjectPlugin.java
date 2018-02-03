package mo.exchange.data.plugin;

import java.util.List;
import mo.exchange.data.ExchangeableProjectData;


public interface ExchangeableProjectPlugin extends ExchangeablePlugin
{
	void setImportdata(List<ExchangeableProjectData> data);
}