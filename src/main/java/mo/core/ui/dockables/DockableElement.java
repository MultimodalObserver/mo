package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;

public class DockableElement extends DefaultSingleCDockable {
    
    String group;
    String id;
    CLocation backupLocation;
    
    public DockableElement(String id, CAction... actions) {
        this(id, null, actions);
        this.id = id;
        
    }
    
    public DockableElement(String id, String groupId, CAction... actions) {
        super(id, actions);
        this.id = id;
        
        setTitleText(id);
        this.group = groupId;
        setCloseable(true);
    }
    
    public DockableElement(String id, String groupId, String title, CAction... actions) {
        super(id, actions);
        this.id = id;
        this.group = groupId;
        setTitleText(title);
        setCloseable(true);
    }
    
    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "[DockE="+id+"]";
    }
    
    public void setBackupLocation(CLocation location) {
        backupLocation = location;
    }
    
    public CLocation getBackupLocation() {
        return backupLocation;
    }
}
