package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.MissingCDockableStrategy;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.SingleCDockableFactory;
import bibliothek.util.xml.XElement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import org.apache.commons.lang3.SerializationUtils;
/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class Test4 implements Serializable {

    private HashMap<String, ArrayList<MyDockable>> dockables;
    private ArrayList<XElement> fakeStorage = new ArrayList<>();
    private CControl control;
    private JFrame frame;

    public Test4() {
        frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        control = new CControl(frame);

        frame.add(control.getContentArea());

        dockables = new HashMap<>();

        MyDockable d = new MyDockable("1.1");
        addFrameDockable(d, "1");
        d.setVisible(true);

        d = new MyDockable("2.1");
        addFrameDockable(d, "2");
        d.setVisible(true);

        d = new MyDockable("1.2");
        addFrameDockable(d, "1");
        d.setVisible(true);

        frame.setVisible(true);
        frame.setSize(400, 400);
        
        XElement e = new XElement("root");
        control.writeXML(e);
        System.out.println(e);
    }

    public Test4(ArrayList<XElement> storage) {
        frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        control = new CControl(frame);
        control.setMissingStrategy(MissingCDockableStrategy.SINGLE);

        frame.add(control.getContentArea());

        //XElement root = new XElement(("root"));
        //control.writeXML(root);
        //System.out.println(root);
        for (XElement xElement : storage) {
            control.readXML(xElement);
        }

        System.out.println(control.getCDockableCount());
        for (int i = 0; i < control.getCDockableCount(); i++) {
            MyDockable d = (MyDockable) control.getCDockable(i);
            System.out.println(d.getUniqueId());
            d.setVisible(true);
        }

        dockables = new HashMap<>();

        frame.setVisible(true);
        frame.setSize(400, 400);
        
        XElement e = new XElement("root");
        control.writeXML(e);
        System.out.println(e);
    }

    public void addFrameDockable(MyDockable dockable, String groupId) {

        if (!dockables.containsKey(groupId))
            dockables.put(groupId, new ArrayList());

        dockables.get(groupId).add(dockable);
        control.addDockable(dockable);
        
    }

    public void saveDockables() throws FileNotFoundException, IOException {
        for (String group : dockables.keySet()) {
            JFrame f = new JFrame();
            CControl c = new CControl(f);
            f.add(c.getContentArea());
            for (MyDockable d : dockables.get(group)) {
                MyDockable copy = new MyDockable(d);
                c.addDockable(copy);
                copy.setVisible(true);
            }
            XElement e = new XElement("root");
            c.writeXML(e);
            //System.out.println(e);
            fakeStorage.add(e);
        }
    }

    public static void main(String[] args) throws InterruptedException, Throwable {
        Test4 t = new Test4();
        t.saveDockables();
        t.frame.dispose();
        t = new Test4(t.fakeStorage);
        t.saveDockables();
    }

    public class MyDockable extends DefaultSingleCDockable {

        long otherData;

        public MyDockable(String id) {
            super(id, id);
            setTitleText(id);
            otherData = System.currentTimeMillis() % 1000;
        }

        public MyDockable(MyDockable d) {
            super(d.getUniqueId());
            this.otherData = d.getOtherData();
            setCloseable(d.isCloseable());
            //setControlAccess(d.getControlAccess());
            setExtendedMode(d.getExtendedMode());

            setFocusComponent(d.getFocusComponent());
            setGrouping(d.getGrouping());
            //System.out.println(">>>" + d.getBaseLocation());
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

}
