package mo.echange.data.importer;

import mo.exchange.data.importer.plugin.ImportProjectPlugin;


// import javax.swing.JFileChooser;
// import java.io.File;
// import javax.swing.filechooser.FileFilter;
// import mo.core.I18n;
import mo.core.filemanagement.project.ProjectManagement;

import mo.core.filemanagement.project.ProjectUtils;
import javax.swing.JPanel;

import mo.core.ui.WizardDialog;



import java.awt.event.ActionListener;

import java.awt.event.ActionEvent;

import mo.core.filemanagement.project.ProjectUtils;

// import mo.exchange.data.importer.visualization.ImportProjectDialog;
import mo.exchange.data.importer.visualization.ImportProjectDialog2;

import mo.exchange.data.visualization.WaitDialog;

// import mo.exchange.data.ExchangeDataWorker;

import javax.swing.SwingWorker;


import mo.organization.StagePlugin;




public class ImportProjectManager
{
	private ImportProjectPlugin plugin;
	private SwingWorker importer;
	private SwingWorker cancelImporter;

	// private WizardDialog dialog;

	// private JPanel panel;

	// private File selectedFile;

	public ImportProjectManager(ImportProjectPlugin plugin) {
		this.plugin = plugin;

		// ImportProjectDialog dialog = new ImportProjectDialog();
		ImportProjectDialog2 dialog = new ImportProjectDialog2();
		boolean finish = dialog.show();
		if (finish) {
			plugin.setOrigin(dialog.getOriginLocation());
			plugin.setDestiny(dialog.getDestinyLocation());
			// plugin.setImportdata(dialog.getData());

			System.out.println("stage con el que trabaja el plugin " + plugin.getStage().getCodeName());

			
			for (StagePlugin p : plugin.getStage().getPlugins()) {
				System.out.println("nombre del plugin = " + p.getName());
				
			}

			// mandar al worker a ejecutar el export
			// ExchangeDataWorker worker = new ExchangeDataWorker(plugin);

			// levantar un wait dialog
			WaitDialog waitDialog = new WaitDialog();
			waitDialog.addButtonPressedEventListener(new WaitDialog.ButtonPressedEventListener() {
				@Override
				public void okButtonPressed() {
					System.out.println("ok button");
					waitDialog.hide();
				}

				@Override
				public void cancelButtonPressed() {
					System.out.println("cancel button");
				}
			});
			waitDialog.enableOkButton();
			waitDialog.show();
		}
	}
}