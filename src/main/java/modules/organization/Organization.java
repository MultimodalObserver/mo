package modules.organization;

import core.Modulable;
import javax.swing.JOptionPane;

public class Organization implements Modulable {

    @Override
    public String getName() {
        return "Organization";
    }

    @Override
    public String getDescription() {
        return "A files and project organization module.";
    }
    
    @Override
    public void init() {
        JOptionPane asd = new JOptionPane("Hola");
    }
}
