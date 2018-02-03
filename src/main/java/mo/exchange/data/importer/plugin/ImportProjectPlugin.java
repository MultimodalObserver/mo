package mo.exchange.data.importer.plugin;

import mo.core.filemanagement.project.Project;
import mo.core.plugin.ExtensionPoint;
import mo.exchange.data.ExchangeableData;
import java.util.List;

import mo.organization.StageModule;

@ExtensionPoint
public interface ImportProjectPlugin extends ImportPlugin
{
	StageModule getStage();
	// StageModule stageOfThePlugin();

}