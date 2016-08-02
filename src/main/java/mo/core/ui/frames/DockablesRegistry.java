package mo.core.ui.frames;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.util.DirectWindowProvider;
import bibliothek.gui.dock.util.WindowProvider;
import bibliothek.gui.dock.util.WindowProviderWrapper;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.core.ui.menubar.IMenuBarItem;
import mo.core.ui.menubar.IMenuBarItemImpl;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.core.ui.IMenuBarItem"
            )
        }
)
public class DockablesRegistry implements IMenuBarItem{
    
    private static DockablesRegistry registry;
    
    private final CControl control;
    
    //private JMenuItem 
    
    // mantiene todos los dockables de la app
    private HashMap<String, DockableElement> dockables;

    private DockablesRegistry() {
        dockables = new HashMap<>();
        control = new CControl();
        
//        PluginRegistry pg = PluginRegistry.getInstance();
//        for (Plugin plugin : pg.getPluginsFor("mo.core.ui.frames.IDockableElement")) {
//            try {
//                IDockableElement dockable = (IDockableElement) plugin.getClazz().newInstance();
//                control.addDockable(dockable.getElement());
//                //dockable.getElement().setVisible(true);
//            } catch (InstantiationException | IllegalAccessException ex) {
//                Logger.getLogger(DockablesRegistry.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }

    }
    
    public static DockablesRegistry getInstance() {
        if ( registry == null ) {
            registry = new DockablesRegistry();
        }
        return registry;
    }
    
    public void setJFrame( JFrame frame ) {
        getInstance();
        control.setRootWindow(new DirectWindowProvider(frame));
    }
    
    public JFrame getMainFrame() {
        return (JFrame) control.getRootWindow().searchWindow();
    }
    
    public CControl getControl() {
        return this.control;
    }

    @Override
    public JMenuItem getItem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getRelativePosition() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getRelativeTo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
