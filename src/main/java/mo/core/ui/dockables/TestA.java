package mo.core.ui.dockables;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CGridArea;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CStation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.AbstractCStation;
import bibliothek.gui.dock.common.intern.CControlAccess;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.common.intern.station.CommonDockStation;
import bibliothek.gui.dock.common.perspective.CStationPerspective;
import bibliothek.gui.dock.station.split.DockableSplitDockTree;
import bibliothek.util.Path;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class TestA {
    
    JFrame frame;
    CControl control;
    
    public void init() {
        frame = new JFrame("App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        control = new CControl(frame);
        
        frame.add(control.getContentArea());

        frame.setSize(new Dimension(400, 400));
        frame.setVisible(true);
    }
    
    public void asd() {
        DefaultSingleCDockable d = new DefaultSingleCDockable("1");
        control.addDockable(d);
        d.setVisible(true);
    }
    
    public void test() {
        
        //CGridArea ga = new CGridArea(control, "-");
        //ga.getStation().dropTree(createLayoutTree().createTree());

        
        SplitDockStation sd = createLayoutTree();
        System.out.println(sd.getBounds());
        for (int i = 0; i < sd.getDockableCount(); i++) {
            System.out.println(sd.getDockable(i));
            System.out.println(sd.getDockable(i).getComponent().getBounds());
            
        }
        
        CGridArea garea = control.createGridArea("area");
        garea.getStation().dropTree(createLayoutTree().createTree());
        
        
        CGrid c = new CGrid(control);
        c.add(0, 0, 1, 1, new DefaultSingleCDockable("as"));
        CContentArea content = control.getContentArea();
        content.deploy(c);
        
        System.out.println(control.getCDockableCount());
        for (int i = 0; i < control.getCDockableCount(); i++) {
            System.out.println(control.getCDockable(i).isShowing());
        }
        System.out.println(control.getStations().size());
        for (CStation<?> station : control.getStations()) {
            System.out.println(station.getTypeId());
        }
        

    }
    
    
    
    private static SplitDockStation createLayoutTree(){
        SplitDockStation station = new SplitDockStation();
        station.setTitleText("Tree");

        Dockable red = new DefaultDockable("red");
        Dockable green =  new DefaultDockable("green");
        Dockable blue = new DefaultDockable("blue");
        Dockable yellow = new DefaultDockable("yellow");
        Dockable cyan = new DefaultDockable("cyan");
        Dockable magenta = (new DefaultSingleCDockable("magenta")).intern();

        DockableSplitDockTree tree = new DockableSplitDockTree();

        DockableSplitDockTree.Key group = tree.put( new Dockable[]{ red, green, blue }, green );
        DockableSplitDockTree.Key bottomRight = tree.horizontal( cyan, magenta, 1.0/3.0 );
        DockableSplitDockTree.Key keyYellow = tree.put( yellow );
        DockableSplitDockTree.Key right = tree.vertical( keyYellow, bottomRight, 0.3 );
        DockableSplitDockTree.Key root = tree.horizontal( group, right, 0.4 );

        tree.root( root );
        station.dropTree( tree );
        
        return station;
    }
    
    public static void main(String[] args) {
        TestA app = new TestA();
        app.init();
        //test.asd();
        app.test();
    }
}
