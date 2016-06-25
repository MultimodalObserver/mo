package mo.core;

import mo.core.plugin.PluginRegistry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    
    private void nonStaticMain(String args[]){
        //try {
            //MainWindow window = new MainWindow();
            //MainPresenter presenter = new MainPresenter(window);
            //presenter.start();
            PluginRegistry.getInstance();
            //PluginRegistry.test();
            //PluginRegistry.testSourceCodeLocation();
            //PluginRegistry.getClasses(Utils.getBaseFolder()+"/plugins/MOPluginTest.jar");
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    public static void main(String args[]){
        App app = new App();
        app.nonStaticMain(args);
    }
}
