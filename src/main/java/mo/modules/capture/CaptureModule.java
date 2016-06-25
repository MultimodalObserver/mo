package mo.modules.capture;

import mo.pruebas.Modulable;
import java.util.Collection;
import javax.swing.JOptionPane;

public class CaptureModule /*implements Modulable*/ {
    
    public CaptureModule(){
    }

    public String getName() {
        return "Capture";
    }

    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void init() {
        JOptionPane.showMessageDialog(null,
    "Eggs are not supposed to be green.");
    }
    
    public void sayHola() {
        JOptionPane op = new JOptionPane("hola desde "+getName());
    }

    public Collection<String> getModuleExtensionInterfaces() {
        String[] interfaces = {"core.modules.capture.CapturePlugable"};
        return java.util.Arrays.asList(interfaces);
    }
}
