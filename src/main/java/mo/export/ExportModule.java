package mo.export;

import mo.organization.ProjectOrganization;
import mo.export.plugin.ExportPlugin;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;

public class ExportModule {
	public final static Logger logger = Logger.getLogger(ExportModule.class.getName());

	private List<ExportPlugin> plugins;

	public ExportModule(List<ExportPlugin> plugins) {
		this.plugins = plugins;
	}

	public void setOrganization(ProjectOrganization organization) {
		for (ExportPlugin p : plugins ) {
			p.setOrganization(organization);
		}
	}

	public void setDataSourcesLocations(List<File> locations) {
		for (ExportPlugin p : plugins ) {
			p.setDataSourcesLocations(locations);
		}
	}

	public void setExportLocationRoot(File root) {
		for (ExportPlugin p : plugins ) {
			p.setExportLocationRoot(root);
		}
	}

	public void export() {
		for (ExportPlugin p : plugins) {
			p.export();
		}
	}

	public void cancelExport() {
		for (ExportPlugin p : plugins) {
			p.cancelExport();
		}
	}
}