package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.location.*;
import bibliothek.util.xml.XAttribute;
import bibliothek.util.xml.XElement;

public class DockableElement extends DefaultSingleCDockable {
    
    String group;
    String id;
    CLocation backupLocation;
    
    public DockableElement() {
        this(Long.toString(System.currentTimeMillis()));
    }
    
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
    
    public XElement getLocationXML() {
        if (getBaseLocation() == null) {
            if (getAutoBaseLocation(false) != null) {
                return getLocationXML(getAutoBaseLocation(false));
            } else if (getAutoBaseLocation(true) != null) {
                return getLocationXML(getAutoBaseLocation(true));
            } else {
                return null;
            }
        }
            
        return getLocationXML(getBaseLocation());
    }
    
    private XElement getLocationXML(CLocation location) {
        XElement xLocation = new XElement("location");
        XAttribute type = new XAttribute("type");
        
        if (location.findRoot() != null) 
            xLocation.addElement("root").setString(location.findRoot());
        
        if (location.findMode() != null)
            xLocation.addElement("mode")
                    .setString(location.findMode().getModeIdentifier().toString());
        
        if (location.findProperty() != null) {
            XElement prop = new XElement("property");
            XAttribute propType = new XAttribute("type");
            propType.setString(
                    location.findProperty().getClass().getSimpleName());
            
            XElement propStore = new XElement("tempStore"); 
            location.findProperty().store(propStore);
            
            for (int i = 0; i < propStore.getElementCount(); i++) {
                prop.addElement(propStore.getElement(i));
            }
            
            prop.addAttribute(type);
            xLocation.addElement(prop);
        }
        
        if ( location instanceof TreeLocationLeaf) {
            type.setString("TreeLocationLeaf");
            TreeLocationLeaf l = (TreeLocationLeaf) location;
        } else if ( location instanceof TreeLocationNode) { 
            //never
            type.setString("TreeLocationNode");
            TreeLocationNode l = (TreeLocationNode) location;
        } else if ( location instanceof TreeLocationRoot) { 
            //never
            type.setString("TreeLocationRoot");
            TreeLocationRoot l = (TreeLocationRoot) location;
        } else if ( location instanceof CExternalizedLocation) {
            type.setString("CExternalizedLocation");
            CExternalizedLocation l = (CExternalizedLocation) location;
        } else if ( location instanceof CMaximalExternalizedLocation) { 
            type.setString("CMaximalExternalizedLocation");
            CMaximalExternalizedLocation l = (CMaximalExternalizedLocation) location;
        } else if ( location instanceof CFlapIndexLocation ) { 
            type.setString("CFlapIndexLocation");
            CFlapIndexLocation l = (CFlapIndexLocation) location;      
        } else if ( location instanceof CMaximizedLocation) {
            type.setString("CMaximizedLocation");
            CMaximizedLocation l = (CMaximizedLocation) location;
        } else if ( location instanceof CRectangleLocation) {
            type.setString("CRectangleLocation");
            CRectangleLocation l = (CRectangleLocation) location;
        } else if ( location instanceof CStackLocation) {
            type.setString("CStackLocation");
            CStackLocation l = (CStackLocation) location;
            xLocation.addElement("index").setInt(l.getIndex());
        } else if ( location instanceof CBaseLocation) { 
            type.setString("CBaseLocation");
            CBaseLocation l = (CBaseLocation) location;
        } else if ( location instanceof CExtendedModeLocation) { 
            type.setString("CExtendedModeLocation");
            CExtendedModeLocation l = (CExtendedModeLocation) location;
        } else if ( location instanceof CFlapLocation) { 
            type.setString("CFlapLocation");
            CFlapLocation l = (CFlapLocation) location;
        } else if ( location instanceof CMinimizeAreaLocation) { 
            type.setString("CMinimizeAreaLocation");
            CMinimizeAreaLocation l = (CMinimizeAreaLocation) location;
        } else if ( location instanceof CMinimizedLocation) { 
            type.setString("CMinimizedLocation");
            CMinimizedLocation l = (CMinimizedLocation) location;
        } else if ( location instanceof CSplitLocation) { 
            type.setString("CSplitLocation");
            CSplitLocation l = (CSplitLocation) location;
        } else if ( location instanceof CContentAreaCenterLocation) { 
            type.setString("CContentAreaCenterLocation");
            CContentAreaCenterLocation l = (CContentAreaCenterLocation) location;
        } else if ( location instanceof CGridAreaLocation) { 
            type.setString("CGridAreaLocation");
            CGridAreaLocation l = (CGridAreaLocation) location;
        } else if ( location instanceof CWorkingAreaLocation) { 
            type.setString("CWorkingAreaLocation");
            CWorkingAreaLocation l = (CWorkingAreaLocation) location;
        } else {
            type.setString("null");
        }
        
        xLocation.addAttribute(type);
        return xLocation;
    }
}
