package mo.exchange.data.importer;

import javax.swing.SwingWorker;
import mo.exchange.data.importer.plugin.ImportPlugin;

public class ImportWorker
{
	private ImportPlugin plugin;

	private SwingWorker importer;
	private SwingWorker cancelImporter;

	// public final static Logger logger = Logger.getLogger(ExportProjectManager.class.getName());

	public ImportWorker(ImportPlugin plugin) {
		this.plugin = plugin;
	}

	private ImportWorker() {
		importer = new SwingWorker<Void,Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // try {
	               //  plugin.execute();
                //     // module.export();
                // } catch(Exception ex) {
                //     logger.log(Level.SEVERE, null, ex);
                // }
                return null;
            }
        };
	}

}