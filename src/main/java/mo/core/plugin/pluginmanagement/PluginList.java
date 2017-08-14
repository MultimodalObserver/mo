package mo.core.plugin.pluginmanagement;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 *
 * @author felo
 */
public class PluginList extends JPanel {
    
    public void refresh(){
    
        System.out.println("Refrescando el visor");
    }
    
    public PluginList(){
        JButton jb = new JButton("Boton temporal");
        add(jb);
    }
    
}
