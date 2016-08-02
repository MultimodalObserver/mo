package mo.core.ui.frames;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.location.CContentAreaCenterLocation;
import bibliothek.gui.dock.common.location.CExtendedModeLocation;
import bibliothek.gui.dock.common.location.CExternalizedLocation;
import bibliothek.gui.dock.common.location.CFlapIndexLocation;
import bibliothek.gui.dock.common.location.CFlapLocation;
import bibliothek.gui.dock.common.location.CGridAreaLocation;
import bibliothek.gui.dock.common.location.CMaximalExternalizedLocation;
import bibliothek.gui.dock.common.location.CMaximizedLocation;
import bibliothek.gui.dock.common.location.CMinimizeAreaLocation;
import bibliothek.gui.dock.common.location.CMinimizedLocation;
import bibliothek.gui.dock.common.location.CRectangleLocation;
import bibliothek.gui.dock.common.location.CSplitLocation;
import bibliothek.gui.dock.common.location.CStackLocation;
import bibliothek.gui.dock.common.location.CWorkingAreaLocation;
import bibliothek.gui.dock.common.location.TreeLocationLeaf;
import bibliothek.gui.dock.common.location.TreeLocationNode;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.util.xml.XAttribute;
import bibliothek.util.xml.XElement;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class LocationUtils {
    
    public static XElement getLocationXML(CDockable dockable) {
        if (dockable.getBaseLocation() == null) {
            if (dockable.getAutoBaseLocation(false) != null) {
                return getLocationXML(dockable.getAutoBaseLocation(false));
            } else if (dockable.getAutoBaseLocation(true) != null) {
                return getLocationXML(dockable.getAutoBaseLocation(true));
            } else {
                return null;
            }
        }
            
        return getLocationXML(dockable.getBaseLocation());
    }
    
    public static XElement getLocationXML(CLocation location) {
        XElement x = new XElement("location");
        XAttribute type = new XAttribute("type");
        
        if (location.findRoot() != null) 
            x.addElement("root").setString(location.findRoot());
        
        if (location.findMode() != null)
            x.addElement("mode")
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
            x.addElement(prop);
        }
        
//        if (location.getParent() != null)
//            x.addElement("parent")
//                    .addElement(getLocationXML(location.getParent()));

        
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
            x.addElement("index").setInt(l.getIndex());
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
        
        x.addAttribute(type);
        return x;
    }
}
