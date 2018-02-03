package mo.exchange.data.plugin;

import java.util.List;
import mo.organization.Participant;
import mo.organization.StageModule;

public interface ExchangeableDataPlugin extends ExchangeablePlugin
{
	StageModule getStageModule();
	void setImportdata(List<Participant> data);			
}