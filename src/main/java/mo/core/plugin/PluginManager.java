package mo.core.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 *
 * @author felo
 */
public class PluginManager extends JPanel {
    
    public void refresh(){
    
        System.out.println("Refrescando el administrador de plugins");
    }
    

    PluginManager(){
        
        add(new JButton("Boton 1"));
        add(new JButton("Boton 2"));
        JButton addPlugin = new JButton("Add new plugin");
        addPlugin.addActionListener(new ActionListener(){            
        
            @Override
            public void actionPerformed(ActionEvent e){
                
                JFileChooser fc = new JFileChooser();
                fc.setMultiSelectionEnabled(true);
                int fcState = fc.showDialog(null, "Select plugin");
                
                if(fcState == JFileChooser.CANCEL_OPTION){
                    System.out.println("Cancelado");
                    
                } else if(fcState == JFileChooser.APPROVE_OPTION){
                    System.out.println("Aprobado");
                    
                } else if(fcState == JFileChooser.ERROR_OPTION){
                    System.out.println("Error");
                }
                
                
                File[] files = fc.getSelectedFiles();
                
                for(File f : files){                    
                    System.out.println(f);
                }
            }        
        });
        
        add(addPlugin);
    }
}
