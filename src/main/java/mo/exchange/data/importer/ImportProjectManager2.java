package mo.echange.data.importer;

// import mo.exchange.data.importer.plugin.ImportProjectPlugin;
// import mo.exchange.data.importer.plugin.ImportProjectPlugin;
import mo.exchange.data.importer.plugino.ImportProjectPlugin;

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


import mo.exchange.data.ExchangeableProjectData;


import java.util.logging.Level;
import java.util.logging.Logger;





import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


import mo.exchange.data.ExchangeableWorker;

import java.beans.PropertyChangeEvent;		// borrame luego
import java.beans.PropertyChangeListener;	// borrame luego



public class ImportProjectManager2 implements PropertyChangeListener
{
	private ImportProjectPlugin plugin;
	private SwingWorker importer;
	private SwingWorker cancelImporter;
	private boolean canceledImport;

	private WaitDialog waitDialog;

	// private WizardDialog dialog;

	// private JPanel panel;

	// private File selectedFile;

	public final static Logger logger = Logger.getLogger(ImportProjectManager2.class.getName());

	public ImportProjectManager2(ImportProjectPlugin plugin) {
		this.plugin = plugin;

		ImportProjectDialog2 dialog = new ImportProjectDialog2();
		boolean finish = dialog.show();
		if (finish) {
			plugin.setOrigin(dialog.getOriginLocation());
			plugin.setDestiny(dialog.getDestinyLocation());
			plugin.setImportdata(dialog.getData()); // arreglar esto

			for (ExchangeableProjectData d : dialog.getData()) {
				System.out.println("el participante = " + d.participant.id);
				for (String folder : d.stageFolders) {
					System.out.println("el folder = " + folder);
					
				}
				
			}

			// System.out.println("stage con el que trabaja el plugin " + plugin.getStage().getCodeName());

			
			// for (StagePlugin p : plugin.getStage().getPlugins()) {
			// 	System.out.println("nombre del plugin = " + p.getName());
			// }

			// mandar al worker a ejecutar el export
			ExchangeableWorker worker = new ExchangeableWorker(plugin);
			worker.addPropertyChangeListener(this);
			worker.execute();

			// ExchangeDataWorker worker = new ExchangeDataWorker(plugin);
			// importer = new SwingWorker // here

			// levantar un wait dialog
			waitDialog = new WaitDialog();
			waitDialog.addButtonPressedEventListener(new WaitDialog.ButtonPressedEventListener() {
				@Override
				public void okButtonPressed() {
					System.out.println("ok button");
					waitDialog.hide();
				}

				@Override
				public void cancelButtonPressed() {
					System.out.println("cancel button");
					waitDialog.setText("Cancelando ...");
					waitDialog.disableOkButton();
					waitDialog.disableCancelButton();
					worker.cancel();
				}
			});
			waitDialog.setText("Importando...");
			waitDialog.disableOkButton();
			waitDialog.enableCancelButton();
			waitDialog.show();
		}
	}

	private void exchangeDone() {
		waitDialog.enableOkButton();
		waitDialog.disableCancelButton();
		waitDialog.setText("Importación lista");
	}

	private void cancelExchangeDone() {
		System.out.println("cancelExchangeDone");
		waitDialog.enableOkButton();
		waitDialog.disableCancelButton();
		waitDialog.setText("Importación cancelada");
	}

	@Override
    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("i'm done from propertyChange");
        if (ExchangeableWorker.ExchangeStatusValue.DONE.equals(evt.getNewValue())) {
        	System.out.println("exchangeDone");
        	exchangeDone();
        }

        if (ExchangeableWorker.CancelExchangeStatusValue.DONE.equals(evt.getNewValue())) {
        	cancelExchangeDone();
        }
    }
}