package mo.core.ui.menubar;

import javax.swing.JMenuItem;

import mo.core.plugin.ExtensionPoint;

@ExtensionPoint
public interface IMenuBarItemProvider {
    
    static final int UNDER  = -2;
    static final int BEFORE = -3;
    static final int AFTER  = -4;  
    
    
    public JMenuItem getItem();
    public int getRelativePosition();
    public String getRelativeTo();
}
