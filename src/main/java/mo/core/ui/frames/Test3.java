package mo.core.ui.frames;

import bibliothek.gui.DockController;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.SingleCDockableFactory;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.grouping.DockableGrouping;
import bibliothek.gui.dock.common.intern.CDockController;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.util.DockUtilities;
import bibliothek.util.xml.XElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class Test3 {
    
    private HashMap<String, ArrayList<MyDockable>> dockables;
    private CControl control;
    private ArrayList<XElement> fakeStorage;
    private JFrame frame;
    
    public Test3() throws InterruptedException {
        frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        control = new CControl(frame);

        frame.add(control.getContentArea());
        
        dockables = new HashMap<>();
        fakeStorage = new ArrayList<>();
        
        MyDockable d = new MyDockable("1.1");
        addFrameDockable(d, "1");
        d.setVisible(true);
        
        d = new MyDockable("2.1");
        addFrameDockable(d, "2");
        d.setVisible(true);
        
        MyDockable dd = d;
        
        d = new MyDockable("1.2");
        addFrameDockable(d, "1");
        d.setLocation(dd.getBaseLocation());
        
        
        d.setVisible(true);
        
        for (int i = 0; i < control.getCDockableCount(); i++) {
            printDockable((DefaultSingleCDockable) control.getCDockable(i));
        }
        
        
        System.out.println(d.toString());
        
        frame.setVisible(true);
        frame.setSize(400,400);
        
    }
    
    public Test3(ArrayList<XElement> storage) {
        JFrame frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        control = new CControl(frame);

        frame.add(control.getContentArea());
        
        for (int i = 0; i < storage.size(); i++) {
            //System.out.println(storage.get(i));
            control.readXML(storage.get(i));
        }
        
        XElement root = new XElement(("root"));
        control.writeXML(root);
        System.out.println(root);
        
        //control.
        
        for (int i = 0; i < control.getCDockableCount(); i++) {
            MyDockable d = (MyDockable) control.getCDockable(i);
            System.out.println(d.getUniqueId());
            d.setVisible(true);
            
        }
        
        dockables = new HashMap<>();
        fakeStorage = new ArrayList<>();
        
        frame.setVisible(true);
        frame.setSize(400,400);
    }
    
    public void addFrameDockable(MyDockable dockable, String groupId) {
        
        if ( !dockables.containsKey(groupId) )
            dockables.put(groupId, new ArrayList());
        
        dockables.get(groupId).add(dockable);
        control.addDockable(dockable);
        
    }
    
    public void saveDockables() {
//        for (String string : dockables.keySet()) {
//            CControl tempControl = new CControl();
//            tempControl.getContentArea();
//            for (MyDockable d : dockables.get(string)) {
//                System.out.println(d.getUniqueId());
//                tempControl.addDockable(new MyDockable(d));
//            }
//            XElement root = new XElement("root");
//            
//            tempControl.writeXML(root);
//            tempControl.destroy();
//            fakeStorage.add(root);
//            System.out.println(root);
//        }
        for (String string : dockables.keySet()) {
            
            XElement root = new XElement("dockables");
            
            for (MyDockable d : dockables.get(string)) {
                XElement dock = new XElement("dockable");
                dock.addElement("id").setString(d.id);
                dock.addElement("title").setString(d.getTitleText());

            }

        }
    }

    public ArrayList<XElement> getFakeStorage() {
        return fakeStorage;
    }

    public static void main(String[] args) throws InterruptedException, Throwable {
        Test3 t = new Test3();
//        Thread.sleep(7000);
//        t.saveDockables();
//        ArrayList<XElement> storage = t.getFakeStorage();
//        t.frame.dispose();
//        //System.out.println(t);
//        t = new Test3(storage);
    }
    
    public class MyDockable extends DefaultSingleCDockable implements Serializable {
        
        String id;
        long otherData;

        public MyDockable(String id) {
            super(id);
            setTitleText(id);
            otherData = System.currentTimeMillis();
        }
        
        public MyDockable(MyDockable d) {
            super(d.getUniqueId());
            setOtherData(d.getOtherData());
            setCloseable(d.isCloseable());
//            setControlAccess(d.getControlAccess());
            setExtendedMode(d.getExtendedMode());

            setFocusComponent(d.getFocusComponent());
            setGrouping(d.getGrouping());
            //System.out.println(">>>"+d.getBaseLocation());
            setLocation(d.getBaseLocation());
            setDefaultLocation(d.getExtendedMode(), d.getBaseLocation());
            setMinimizedSize(d.getMinimizedSize());

            setTitleIcon(d.getTitleIcon());
            setTitleIconHandling(d.getTitleIconHandling());
            setTitleText(d.getTitleText());
            setTitleToolTip(d.getTitleToolTip());
            
//            //setVisible(d.isVisible());
            setWorkingArea(d.getWorkingArea());

            setExternalizable(d.isExternalizable());
            setMaximizable(d.isMaximizable());
            setMinimizable(d.isMinimizable());
            setResizeLocked(d.isResizeLocked());
            setResizeLockedHorizontally(d.isResizeLockedHorizontally());
            setResizeLockedVertically(d.isResizeLockedVertically());
            setSingleTabShown(d.isSingleTabShown());
            setStackable(d.isStackable());
            setSticky(d.isSticky());
            setStickySwitchable(d.isStickySwitchable());
            setTitleShown(d.isTitleShown());
        }

        public long getOtherData() {
            return otherData;
        }

        public void setOtherData(long otherData) {
            this.otherData = otherData;
        }
    }
    
    
    public void printDockable(DefaultSingleCDockable d) {
        System.out.println("/´´´´´´´");
        System.out.println("| id            : "+d.getUniqueId());
        System.out.println("| title         : "+d.getTitleText());
        System.out.println("| title tooltip : "+d.getTitleToolTip());
        System.out.println("| base location : "+d.getBaseLocation());
        CLocation c = d.getBaseLocation();
        System.out.println("|  \\ root     : "+c.findRoot());
        System.out.println("|  \\ mode     : "+c.findMode());
        System.out.println("|       \\ iden: "+c.findMode().getModeIdentifier());
        System.out.println("|  \\ property : "+c.findProperty());
        System.out.println("|        \\ : "+c.findProperty().getFactoryID());
        System.out.println("|        \\ : "+c.findProperty().getSuccessor());
        System.out.println("|  \\ parent   : "+c.getParent());
        System.out.println("| ext mode      : "+d.getExtendedMode().getModeIdentifier());
        System.out.println("| ccontrol      : "+d.getControl());
        System.out.println("\\________");
        //System.out.println("| : "+d);
        System.out.println(DockUtilities.getPropertyChain(d.intern()));
    }
}
