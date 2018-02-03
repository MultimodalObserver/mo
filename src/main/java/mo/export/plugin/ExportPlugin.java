package mo.export.plugin;

import mo.organization.ProjectOrganization;
import java.io.File;
import java.util.List;

public interface ExportPlugin
{
	String getCodeName();
	String getName();
	String getDescription();

	void setOrganization(ProjectOrganization organization);
	void setDataSourcesLocations(List<File> locations);
	void setExportLocationRoot(File root);

	// void setOrigin(File origin);
	// void setDestiny(File destiny);
	// void setImportdata(List<ExchangeableData> data);
	




	void export();
	void cancelExport();
}