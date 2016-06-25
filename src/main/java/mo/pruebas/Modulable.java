package mo.pruebas;

import java.util.Collection;
import javax.swing.JFrame;

public interface Modulable {

    /**
     *
     * @return
     */
    String getName();
    
    String getDescription();
    
    void init();
    
    Collection<String> getModuleExtensionInterfaces();
    
    boolean isSingleton();
    
    void setParentFrame(JFrame frame);
}
