package core;

import bibliothek.gui.dock.common.CControl;
import java.awt.Frame;
import java.util.ArrayList;
import javax.swing.JFrame;

/**
 *
 * @author Celso
 */
public class PanelManager {
    ArrayList<Panel> panels;
    JFrame frame;
    CControl control;
    
    public PanelManager(JFrame frame){
        this.frame = frame;
        control = new CControl(this.frame);
        //Object o = Frame.FACTORY;
    }
    
    public Panel getNewPanel(String name){
        Panel p = new Panel(frame, name);
        panels.add(p);
        return p;
    }
    
    public void addPanel(Panel panel){
        if (panels == null){
            panels = new ArrayList<>();
        }
        panels.add(panel);
    }
}
