package modules.capture;

import core.Modulable;
import javax.swing.JOptionPane;

public class CaptureModule implements Modulable {
    
    public CaptureModule(){
    }

    @Override
    public String getName() {
        return "Capture";
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void init() {
        JOptionPane.showMessageDialog(null,
    "Eggs are not supposed to be green.");
    }
    
    public void sayHola() {
        JOptionPane op = new JOptionPane("hola desde "+getName());
    }
}
