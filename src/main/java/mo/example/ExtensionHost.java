package mo.example;

import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Celso
 */
public class ExtensionHost {
    
    public ExtensionHost(){
        
    }
    
    public static void main(String[] args) throws InterruptedException{
        List<Plugin> myPlugins = PluginRegistry.getInstance().getPluginsFor("example.IExtPointExample");
        //Thread.sleep(5000);
        for (Plugin myPlugin : myPlugins) {
            try {
                Class<?> c = myPlugin.getClazz();
                System.out.println(c);
                IExtPointExample o = (IExtPointExample)c.newInstance();
                o.SayHi();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ExtensionHost.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
