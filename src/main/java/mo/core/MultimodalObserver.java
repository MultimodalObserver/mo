package mo.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import mo.core.plugin.PluginRegistry;
import mo.core.utils.Utils;

public class MultimodalObserver {
    
    public PluginRegistry pr;
    
    public static final String APP_PREFERENCES_FILE = 
            Utils.getBaseFolder()+"/preferences.xml";
    
    private void nonStaticMain(String args[]){
        pr = PluginRegistry.getInstance();
        //try {
        //DockablesRegistry.getInstance();
        MainWindow window = new MainWindow();
        MainPresenter presenter = new MainPresenter(window);
        presenter.start();
        
        //for (Plugin plugin : PluginRegistry.getInstance().getPlugins()) {
        //    System.out.println(plugin.toString());
        //}
            //System.out.println("-----------------------------------------------");
            
            //System.out.println("-----------------------------------------------");
            //System.out.println(PluginRegistry.getInstance().getPlugins().size());
            //PluginRegistry.test();
            //PluginRegistry.testSourceCodeLocation();
            //PluginRegistry.getClasses(Utils.getBaseFolder()+"/plugins/MOPluginTest.jar");
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    public static void main(String args[]){

        Logger l = Logger.getLogger("");
        l.setLevel(Level.INFO);
        l.getHandlers()[0].setLevel(Level.INFO);
        
        MultimodalObserver app = new MultimodalObserver();
        app.nonStaticMain(args);
    }
}
