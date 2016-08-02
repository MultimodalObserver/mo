package mo.core.ui.frames;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class DockableElement extends DefaultSingleCDockable {
    
    String group;
    String id;
    
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
    
    
}
