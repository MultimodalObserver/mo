package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.location.*;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.util.xml.XAttribute;
import bibliothek.util.xml.XElement;
import java.util.List;
import static mo.core.ui.dockables.DockablesTreeRecreator.findDockablesInControlWithBounds;

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
    
    public void saveBackupLocation() {
        this.backupLocation = getLocation();
    }
    
    public void restoreBackupLocation() {
        this.setLocation(backupLocation);
    }
    
    public XElement getLocationXML() {
        return getLocationXML(getLocation());
    }

    public CLocation getLocation() {
        if (getBaseLocation() == null) {
            if (getAutoBaseLocation(false) != null) {
                return getAutoBaseLocation(false);
            } else if (getAutoBaseLocation(true) != null) {
                return getAutoBaseLocation(true);
            } else {
                return new CBaseLocation();
            }
        } else {
            return getBaseLocation();
        }
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
    
    public void setLocationFromXml(CControl control, XElement xmlLocationInfo) {
        String type = xmlLocationInfo.getAttribute("type").getValue();

        if (type.equals("CExternalizedLocation")) {
            //System.out.println("externalized");
            int x, y, w, h;
            XElement prop = xmlLocationInfo.getElement("property");
            x = prop.getElement("x").getInt();
            y = prop.getElement("y").getInt();
            w = prop.getElement("width").getInt();
            h = prop.getElement("height").getInt();

            if (xmlLocationInfo.getElement("mode").getValue().endsWith("maximized")) {
                //System.out.println("  maximized");
                setLocation(CLocation.external(x, y, w, h));
                //TODO maximize
            } else {
                //System.out.println("  no maximized");
                setLocation(CLocation.external(x, y, w, h));

            }
        } else if (type.equals("CMaximalExternalizedLocation")) {
            //never happends?
        } else if (type.equals("CStackLocation")) {
            //System.out.println("  cstacklocation");
            String mode = xmlLocationInfo.getElement("mode").getValue();
            if (mode.endsWith("externalized")) {
                //System.out.println("   external");
                XElement property = xmlLocationInfo.getElement("property");
                int x = property.getElement("x").getInt();
                int y = property.getElement("y").getInt();
                int w = property.getElement("width").getInt();
                int h = property.getElement("height").getInt();
                List<DockableElement> l = findDockablesInControlWithBounds(control, x, y, w, h);
                //System.out.println("l size: " + l.size());
                if (l.isEmpty()) {
                    //System.out.println("no l");
                    setLocation(CLocation.external(x, y, w, h));

                } else {
                    //System.out.println("si l : " + l.get(0).id);
                    setLocationsAside(l.get(0));
                }
            } else if (mode.endsWith("normal")) {
                //System.out.println("   normal");
            }
        } else if (type.equals("CFlapIndexLocation")) {
            setLocation(CLocation.base());
            setExtendedMode(ExtendedMode.MINIMIZED);
        } else if (type.equals("TreeLocationLeaf")) {
            //delegated to tree
        } else {
            setLocation(CLocation.base());
        }
    }

}
