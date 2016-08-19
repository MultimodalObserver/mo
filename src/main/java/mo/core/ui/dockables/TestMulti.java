package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.MissingCDockableStrategy;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.util.xml.XElement;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class TestMulti {
    
    private HashMap<String, ArrayList<MyDockable>> dockables;
    private CControl control;
    private ArrayList<XElement> fakeStorage;
    private JFrame frame;
    
    public TestMulti() throws InterruptedException {
        frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        control = new CControl(frame);

        frame.add(control.getContentArea());
        
        dockables = new HashMap<>();
        fakeStorage = new ArrayList<>();
        
        MyFactory factory = new MyFactory();
        
        control.addMultipleDockableFactory("factory", factory);
        
        MyDockable d = new MyDockable("1.1", factory);
        addFrameDockable(d, "1");
        d.setVisible(true);
        
        d = new MyDockable("2.1", factory);
        addFrameDockable(d, "2");
        d.setVisible(true);
        
        d = new MyDockable("1.2", factory);
        addFrameDockable(d, "1");
        d.setVisible(true);
        
        for (int i = 0; i < control.getCDockableCount(); i++) {
            System.out.println(((MyDockable)control.getCDockable(i)).getTitleText());
        }
        
        frame.setVisible(true);
        
        //DockController c = new DockController();
        frame.setSize(400,400);
        
        Thread.sleep(4000);
        
        XElement e = new XElement("root");
        control.writeXML(e);
        System.out.println(e);
    }
    
    public TestMulti(ArrayList<XElement> storage) {
        frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        control = new CControl(frame);
        System.out.println(control.getMissingStrategy().equals(MissingCDockableStrategy.PURGE));
        control.setMissingStrategy(MissingCDockableStrategy.STORE);

        frame.add(control.getContentArea());
        
        MyFactory factory = new MyFactory();
        
        control.addMultipleDockableFactory("factory(1)", new MyFactory());
        control.addMultipleDockableFactory("factory(2)", new MyFactory());
        
        for (int i = 0; i < storage.size(); i++) {
            //System.out.println(storage.get(i));
            control.readXML(storage.get(i));
            
        }
        
        //MyDockable m = (MyDockable) control.getMultipleDockable("1.1");
        //System.out.println(m.getId());

        for (int i = 0; i < control.getCDockableCount(); i++) {
            MyDockable d = (MyDockable) control.getCDockable(i);
            System.out.println(d.getId());
            d.setVisible(true);
            
        }
        
        XElement root = new XElement("root");
            control.writeXML(root);
            System.out.println(root);

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
        MyFactory factory = new MyFactory();
        for (String string : dockables.keySet()) {
            CControl tempControl = new CControl();
            
            tempControl.addMultipleDockableFactory("factory("+string+")", factory);
            tempControl.getContentArea();
            for (MyDockable d : dockables.get(string)) {
                //System.out.println("id1>"+d.getId());
                MyDockable copy = new MyDockable(d, factory);
                //System.out.println("id>"+copy.getId());
                tempControl.addDockable( copy );
            }
            XElement root = new XElement("root");
            
            tempControl.writeXML(root);
            tempControl.destroy();
            fakeStorage.add(root);
            //System.out.println(root);
        }
    }

    public ArrayList<XElement> getFakeStorage() {
        return fakeStorage;
    }

    public static void main(String[] args) throws InterruptedException, Throwable {
        TestMulti t = new TestMulti();
        Thread.sleep(3000);
        t.saveDockables();
        ArrayList<XElement> storage = t.getFakeStorage();
        t.frame.dispose();
        //System.out.println(t);
        t = new TestMulti(storage);
    }
    
    public class MyDockable extends DefaultMultipleCDockable {
        
        private String id;
        private long otherData;

        public MyDockable(String id, MyFactory factory) {
            super(factory);
            System.out.println("mydock(id,factory)");
            this.id = id;
            setTitleText(id);
            otherData = System.currentTimeMillis() % 1000;
        }
        
        public MyDockable(MyDockable d, MyFactory factory) {
            super(factory);
            System.out.println("mydock(dockable,factory)");
            this.id = d.getId();
            this.otherData = d.getOtherData();
            setCloseable(d.isCloseable());
//            setControlAccess(d.getControlAccess());
            setExtendedMode(d.getExtendedMode());

            setFocusComponent(d.getFocusComponent());
            setGrouping(d.getGrouping());
            System.out.println(">>>"+d.getBaseLocation());
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

        private MyDockable(MyLayout layout, MyFactory factory) {
            super(factory);
            System.out.println("mydock3");
            
            this.id = layout.getId();
            this.otherData = layout.getOtherData();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getOtherData() {
            return otherData;
        }

        public void setOtherData(long otherData) {
            this.otherData = otherData;
        }

        private MyLayout getLayout() {
            return new MyLayout(id, otherData);
        }
    }
    
    public class MyFactory implements MultipleCDockableFactory <MyDockable,MyLayout>{

        @Override
        public MyLayout write(MyDockable dockable) {
            System.out.println("factory.write");
            return dockable.getLayout();
        }

        @Override
        public MyDockable read(MyLayout layout) {
            System.out.println("factory.read "+layout.getId());
            return new MyDockable(layout, this);
        }

        @Override
        public boolean match(MyDockable dockable, MyLayout layout) {
            System.out.println("factory.match");
            return dockable.getId().compareTo(layout.getId()) == 0;
        }

        @Override
        public MyLayout create() {
            System.out.println("factory.create");
            return new MyLayout();
        }
    }
    
    public class MyLayout implements MultipleCDockableLayout {
        
        private String id;
        private long otherData;
        
        public MyLayout() {
            System.out.println("layout()");
        }
        
        public MyLayout(String id, long otherData) {
            System.out.println("layout1(id,other)");
            this.id = id;
            this.otherData = otherData;
        }

        @Override
        public void writeStream(DataOutputStream out) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void readStream(DataInputStream in) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void writeXML(XElement element) {
            System.out.println("layout.write");
            element.addElement("id").setString(id);
            element.addElement("other").setLong(otherData);
        }

        @Override
        public void readXML(XElement element) {
            System.out.println("layout.readxml "+element);
            this.id = element.getElement("id").getString();
            this.otherData = element.getElement("other").getLong();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getOtherData() {
            return otherData;
        }

        public void setOtherData(long otherData) {
            this.otherData = otherData;
        }
    }
}
