package mo.core.ui.dockables;

import bibliothek.gui.DockController;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.StackDockStation;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CStation;
import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.station.CSplitDockStation;
import bibliothek.gui.dock.common.intern.station.CommonStationDelegate;
import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.location.CExternalizedLocation;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.layout.DockableProperty;
import bibliothek.gui.dock.station.split.DockableSplitDockTree;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class Test51 {
    JFrame frame;
    CControl control;
    
    XElement saved;
    
    DockController controller;
    File file = new File("elements.xml");
    private HashMap<String, ArrayList<DefaultSingleCDockable>> singleDockables;
    private HashMap<String, ArrayList<DefaultMultipleCDockable>> multiDockables;
    
    private List<CExternalizedLocation> externalStacks;
    
    private LocationTree frameTree; 
    
    private Integer findInProperty(String propertyName, DockableProperty property) {
        XElement store = new XElement("store");
        property.store(store);
        for (int j = 0; j < store.getElementCount(); j++) {
            XElement current = store.getElement(j);
            if (current.getName().compareTo(propertyName) == 0) {
                return current.getInt();
            }
        }
        return null;
    }
    
    public List<DockableElement> findWithBounds(int x, int y, int w, int h) {
        ArrayList<DockableElement> result = new ArrayList<>();
        for (int i = 0; i < control.getCDockableCount(); i++) {
            boolean xtrue = false, ytrue = false, wtrue = false, htrue = false;
            DockableElement d = (DockableElement) control.getCDockable(i);
            System.out.println(d.id);
            DockableProperty p;
            if (d.getBaseLocation()!= null) {
                p = d.getBaseLocation().findProperty();
            } else if (d.getAutoBaseLocation(false) != null) {
                p = d.getAutoBaseLocation(false).findProperty();
            } else if (d.getAutoBaseLocation(true) != null){
                p = d.getAutoBaseLocation(true).findProperty();
            } else {
                continue;
            }
            XElement store = new XElement("store");
            System.out.println(p);
            p.store(store);
            for (int j = 0; j < store.getElementCount(); j++) {
                XElement current = store.getElement(j);
                int val;
                try {
                    val = current.getInt();
                
                    if (current.getName().compareTo("x") == 0 &&
                            current.getInt() == x) {
                        xtrue = true;
                    } else if (current.getName().compareTo("y") == 0 && 
                            current.getInt() == y) {
                        ytrue = true;
                    } else if (current.getName().compareTo("width") == 0 && 
                            current.getInt() == w) {
                        wtrue = true;
                    } else if (current.getName().compareTo("height") == 0 && 
                            current.getInt() == h) {
                        htrue = true;
                    }
                } catch (Exception ex) {
                    //Logger.getLogger(Test5.class.getName()).log(Level.SEVERE, null, ex);    
                }
            }

            if (xtrue && ytrue && wtrue && htrue)
                    result.add(d);
        }
        return result;
    }
    
    public void init() {
        externalStacks = new ArrayList<>();
        
        frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        control = new CControl(frame);
        
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("xml");
        JMenuItem item = new JMenuItem("imprime");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < control.getCDockableCount(); i++) {
                    CDockable d = control.getCDockable(i);
                    //printLocation(d);
                    System.out.println(LocationUtils.getLocationXML(d));
                }
            }
        });
        bar.add(menu);
        menu.add(item);
        item = new JMenuItem("save");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                XElement xe = new XElement("dockables");
                XElement root = new XElement("root");
                System.out.println(control.getCDockableCount()+" to save");
                for (int i = 0; i < control.getCDockableCount(); i++) {
                    if ( control.getCDockable(i) instanceof DockableElement) {
                        
                        DockableElement d = (DockableElement) control.getCDockable(i);
                        System.out.println(d.getId());
                        XElement dock = new XElement("dockable");
                        dock.addElement("id").setString(d.getId());
                        dock.addElement(LocationUtils.getLocationXML(d));
                        xe.addElement(dock);
                    }
                }
                root.addElement(xe);
                saved = root;
                
                OutputStream os = null;
                try {
                    os = new FileOutputStream(file);             
                    XIO.writeUTF(saved, os);
                    
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Test51.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Test51.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Test51.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        menu.add(item);
        item = new JMenuItem("load");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    XElement xe = XIO.readUTF(in);

                    XElement doc = xe.getElement("dockables");
                    XElement[] docks = doc.children();
                    
                    
                    
                    for (XElement dock : docks) {

                        
                        
                        //de.setLocation(getLocationFromXml(dock.getElement("location")));
                        
                        if (!dock.getElement("location").getAttribute("type").getString().equals("TreeLocationLeaf")) {
                            
                            DockableElement de = new DockableElement(dock.getElement("id").getValue());
                            de.setTitleText(dock.getElement("id").getValue());
                            control.addDockable(de);
                            
                            setLocationFromXml(de, dock.getElement("location"));
                            de.setVisible(true);
                        }
                            
                        
                        
                    }
                    
                    recreateTree(docks);
                    
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Test51.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Test51.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Test51.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            

            private CLocation getLocationFromXml(XElement dock) {
                System.out.println("loaded: "+dock);
                
                String type = dock.getAttribute("type").getValue();
                
                if (type.compareTo("CExternalizedLocation") == 0) {
                    System.out.println("externalized");
                    int x,y,w,h;
                    XElement prop = dock.getElement("property");
                    x = prop.getElement("x").getInt();
                    y = prop.getElement("y").getInt();
                    w = prop.getElement("width").getInt();
                    h = prop.getElement("height").getInt();
                    
//                    if (dock.getElement("mode").getValue().endsWith("maximized")){
//                        System.out.println("  maximized");
//                        System.out.println(CLocation.maximized(x, y, w, h));
//                        return CLocation.maximized(x, y, w, h);
//                    }else{
                        System.out.println("  no maximized");
                        return  CLocation.external(x, y, w, h);
                    //}
                }
                
                if (type.compareTo("CMaximalExternalizedLocation") == 0) {
                    
                }
                
                if (type.compareTo("CStackLocation") == 0) {
                    String mode = dock.getElement("mode").getValue();
                    if (mode.compareTo("external") == 0) {
                        XElement property = dock.getElement("property");
                        int x = property.getElement("x").getInt();
                        int y = property.getElement("y").getInt();
                        int w = property.getElement("w").getInt();
                        int h = property.getElement("h").getInt();
                        List<DockableElement> l = findWithBounds(x, y, w, h);
                        if (l.isEmpty()) {
                            return CLocation.external(x, y, h, h);
                        } else {
                            return CLocation.external(x, y, h, h);
                        }
                    }
                }
                
                return new CBaseLocation().normal();
            }
        });
        menu.add(item);
        
        JMenu menu2 = new JMenu("dockables");
        JMenuItem addDock = new JMenuItem("add dock");
        addDock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int count = control.getCDockableCount();
                count++;
                DockableElement de = new DockableElement(Integer.toString(count));
                de.setTitleText(de.getId());
                control.addDockable(de);
                de.setVisible(true);
                System.out.println("hola");
            }
        });
        menu2.add(addDock);
        JMenuItem del = new JMenuItem("delete all");
        del.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < control.getCDockableCount(); i++) {
                    //if (control.getCDockable(i) instanceof SingleCDockable) 
                    control.removeDockable((SingleCDockable) control.getCDockable(i));
                }
            }
        });
        menu2.add(del);
        
        bar.add(menu2);
        
        
        
        frame.setJMenuBar(bar);
        frame.add(control.getContentArea());
        
        //CWorkingArea work = control.createWorkingArea("work" );
        
//        CGrid grid = new CGrid( control ); 
//        grid.add(0, 0, 1, 2, new DockableElement( "0" ) );
//        grid.add(3, 1, 1, 1, new DockableElement( "1" ) );
//        grid.add(0, 2, 4, 1, new DockableElement( "2" ) );
//        grid.add(2, 1, 1, 1, new DockableElement( "3" ) );
//        grid.add(2, 0, 2, 1, new DockableElement( "4" ) );
//        grid.add(1, 0, 1, 2, new DockableElement( "5" ) );
//        control.getContentArea().deploy(grid);
          
//        work.deploy(grid);
//        work.setLocation( CLocation.base().normal() );
//        work.setVisible( true );
        
        
        //control.
        
        frame.setLocation(200,200);
        frame.setSize(350, 350);
        frame.setVisible(true);
    }
    
    public void initEnd(){
        
    }
    
    private void setLocationFromXml(DockableElement dockable, XElement xmlLocationInfo) {
        String type = xmlLocationInfo.getAttribute("type").getValue();

        if (type.compareTo("CExternalizedLocation") == 0) {
            System.out.println("externalized");
            int x,y,w,h;
            XElement prop = xmlLocationInfo.getElement("property");
            x = prop.getElement("x").getInt();
            y = prop.getElement("y").getInt();
            w = prop.getElement("width").getInt();
            h = prop.getElement("height").getInt();

            if (xmlLocationInfo.getElement("mode").getValue().endsWith("maximized")){
                System.out.println("  maximized");
                //System.out.println(CLocation.maximized(x, y, w, h));
                dockable.setLocation(CLocation.external(x, y, w, h));
                //TODO maximize
            }else{
                System.out.println("  no maximized");
                dockable.setLocation(CLocation.external(x, y, w, h));

            }
        }
        else
        if (type.compareTo("CMaximalExternalizedLocation") == 0) {
            //never happends?
        }
        else
        if (type.compareTo("CStackLocation") == 0) {
            System.out.println("cstacklocation");
            String mode = xmlLocationInfo.getElement("mode").getValue();
            if (mode.endsWith("externalized")) {
                System.out.println(" external");
                XElement property = xmlLocationInfo.getElement("property");
                int x = property.getElement("x").getInt();
                int y = property.getElement("y").getInt();
                int w = property.getElement("width").getInt();
                int h = property.getElement("height").getInt();
                List<DockableElement> l = findWithBounds(x, y, w, h);
                System.out.println("l size: "+ l.size());
                if (l.isEmpty()) {
                    System.out.println("no l");
                    dockable.setLocation(CLocation.external(x, y, w, h));

                } else {
                    System.out.println("si l : "+l.get(0).id);
                    dockable.setLocationsAside(l.get(0));
                }
            }
        } else if (type.compareTo("CFlapIndexLocation") == 0) {
            dockable.setLocation(CLocation.base());
            dockable.setExtendedMode(ExtendedMode.MINIMIZED);
        } else if (type.equals("TreeLocationLeaf")) {
            //delegated to tree
        }
        else
            dockable.setLocation(CLocation.base());
    }
    
    public void init2() {
        frame = new JFrame("Test");
        controller = new DockController();
        controller.setRootWindow(frame);
        
        
        
    }
    public void recreateTree(XElement []dockables) {
        
        HashMap<Long,List<LocationNode>> treesByRoot = new HashMap<>();

        for (XElement dockable : dockables) {
            XElement location = dockable.getElement("location");
            XElement property = location.getElement("property");
            XElement[] nodes = property.getElements("node");
            XElement leaf = property.getElement("leaf");
            if (leaf != null) {

                LocationNode node = new LocationNode();
                
                node.dock = new DockableElement(dockable.getElement("id").getString());

                node.id = leaf.getAttribute("id").getLong();
                
                String locationStr = 
                        nodes[nodes.length - 1].getAttribute("location")
                                .getString();
                
                node.location = locationFromString(locationStr);
                
                node.size = nodes[nodes.length - 1].getAttribute("size")
                        .getFloat();
                
                LocationNode lastNode = node;
                
                for (int j = nodes.length-1; j > -1; j--) {

                    LocationNode n = new LocationNode();
                    n.id = nodes[j].getAttribute("id").getLong();
                    
                    if (j > 0)
                        n.location = locationFromString(nodes[j - 1]
                                .getAttribute("location").getString());
                    
                    if (j > 0)
                        n.size = nodes[j - 1].getAttribute("size").getFloat();
                    
                    Location childLoc = lastNode.location;
                                        
                    if (null != childLoc) switch (childLoc) {
                        case TOP:
                            n.childrenOrientation = ChildrenOrientation.VERTICAL;
                            n.firstChild = lastNode;
                            break;
                        case BOTTOM:
                            n.childrenOrientation = ChildrenOrientation.VERTICAL;
                            n.secondChild = lastNode;
                            break;
                        case LEFT:
                            n.childrenOrientation = ChildrenOrientation.HORIZONTAL;
                            n.firstChild = lastNode;
                            break;
                        case RIGHT:
                            n.childrenOrientation = ChildrenOrientation.HORIZONTAL;
                            n.secondChild = lastNode;
                            break;
                        default:
                            break;
                    }
                    
                    lastNode = n;
                }
                
                //printNode(lastNode);

                if ( !treesByRoot.containsKey(lastNode.id)) {
                    treesByRoot.put(lastNode.id, new ArrayList<>());
                }
                
                treesByRoot.get(lastNode.id).add(lastNode);
            }
        }
        
        ArrayList<LocationNode> trees = new ArrayList<>();
        
        for (Long id : treesByRoot.keySet()) {
            LocationNode tree = null;
            for (LocationNode node : treesByRoot.get(id)) {
                
                if (tree == null) {
                    tree = node;
                } else {
                    LocationNode currentMain = tree;
                    LocationNode currentNew = node;
                    boolean added = false;
                    while (!added) {
                        if (currentNew.firstChild != null) {
                            currentNew = currentNew.firstChild;
                            if (currentMain.firstChild != null) {
                                currentMain = currentMain.firstChild;
                            } else {
                                currentMain.firstChild = currentNew;
                                currentMain.firstChild.parent = currentMain;
                                added = true;
                            }
                        } else if (currentNew.secondChild != null) {
                            currentNew = currentNew.secondChild;
                            if (currentMain.secondChild != null) {
                                currentMain = currentMain.secondChild;
                            } else {
                                currentMain.secondChild = currentNew;
                                currentMain.secondChild.parent = currentMain;
                                added = true;
                            }
                        }
                    }
                }
            }
            printNode(tree);
            trees.add(tree);
        }
        
        CGrid g = createGrid(control, trees.get(0));
        control.getContentArea().deploy(g);
        
        XElement xe = new XElement("r");
        control.writeXML(xe);
        System.out.println(xe);
    }

    public CGrid createGrid(CControl control, LocationNode root) {
        CGrid grid = new CGrid(control);
        addDockableFromTree(grid, root, 0, 0, 1, 1);
        return grid;
    }
    
    public void addDockableFromTree(CGrid grid, LocationNode node, double x, double y, double w, double h) {
        
        //System.out.format("(%.2f %.2f) %.2f %.2f %s%n" , x, y, w, h, node);
        
        if (node.parent != null) {
            if (node.parent.childrenOrientation.equals(ChildrenOrientation.HORIZONTAL)) {
                w = node.size * w;
            } else {
                h = node.size * h;
            }
            
        }
        
        if (node.dock != null) {
            
            //System.out.println("++ "+node.dock+" ("+x+" "+y+") "+w+" "+h);
            System.out.format("  ++ (%.2f %.2f) %.2f %.2f %s%n" , x, y, w, h, node.dock );
            grid.add(x, y, w, h, node.dock);
            
        } else {

            if (node.firstChild != null){
                //System.out.println("-  "+node.firstChild+" ("+x+" "+y+") "+w+" "+h);
                System.out.format("  -  (%.2f %.2f) %.2f %.2f %s%n" , x, y, w, h, node.firstChild );
                addDockableFromTree(grid, node.firstChild, x, y, w, h);
            }
            
            if (node.secondChild != null) {
                
                if (node.firstChild != null){
                    if (node.childrenOrientation.equals(ChildrenOrientation.HORIZONTAL)) {
                        x += node.firstChild.size * w;
                    } else {
                        y += node.firstChild.size * h;
                    }
                }

                //System.out.println("-- "+node.secondChild+" ("+x+" "+y+") "+w+" "+h);
                System.out.format("  -- (%.2f %.2f) %.2f %.2f %s%n" , x, y, w, h, node.secondChild );
                addDockableFromTree(grid, node.secondChild, x, y, w, h);
            }
        }
    }
    
    public SplitDockStation createDockablesTree(LocationNode tree) {
        //CommonStationDelegate
        SplitDockStation station = new SplitDockStation();

        
        DockableSplitDockTree sdt = new DockableSplitDockTree();
        sdt.root(getNodeKey(tree, sdt));
        station.dropTree(sdt);
        System.out.println(station);
        return new CSplitDockStation( (CommonStationDelegate<CSplitDockStation>) station);
    }
    
    public DockableSplitDockTree.Key getNodeKey(LocationNode node, DockableSplitDockTree t) {
        System.out.println(node + " " + (node != null));
        if (node.dock != null) {
            control.addDockable(node.dock);
            return t.put(node.dock.intern());
        } else {
            if (node.childrenOrientation == ChildrenOrientation.HORIZONTAL) {

                if (node.firstChild != null && node.secondChild != null) {
                    return t.horizontal(getNodeKey(node.firstChild, t), 
                            getNodeKey(node.secondChild, t), 
                            node.firstChild.size );
                } else if (node.firstChild != null) {
                    return getNodeKey(node.firstChild, t);
                } else {
                    return getNodeKey(node.secondChild, t);
                }
            } else {
                if (node.firstChild != null && node.secondChild != null) {
                    return t.vertical(getNodeKey(node.firstChild, t), 
                            getNodeKey(node.secondChild, t), 
                            node.firstChild.size );
                } else if (node.firstChild != null) {
                    return getNodeKey(node.firstChild, t);
                } else {
                    return getNodeKey(node.secondChild, t);
                }    
            } 
        }
    }
    
    public Location locationFromString(String location) {
        switch (location) {
            case "TOP":
                return Location.TOP;
            case "BOTTOM":
                return Location.BOTTOM;
            case "LEFT":
                return Location.LEFT;
            case "RIGHT":
                return Location.RIGHT;
            default:
                return null;
        }
    }
    
    public void testA() {
        
        
        DefaultSingleCDockable d = new DefaultSingleCDockable("1-1");
        d.setTitleText("1-1");
        control.addDockable(d);
        d.setVisible(true);
        
        d = new DefaultSingleCDockable("1-2");
        d.setTitleText("1-2");
        control.addDockable(d);
        d.setVisible(true);
        
        d = new DefaultSingleCDockable("1-3");
        d.setTitleText("1-3");
        control.addDockable(d);
        d.setVisible(true);
        
        //control.getController().
    }
    
    public void testB() {
        DockableElement d = new DockableElement("1-1");
        d.setTitleText("1-1");
        control.addDockable(d);
        d.setVisible(true);
        
        d = new DockableElement("1-2");
        d.setTitleText("1-2");
        control.addDockable(d);
        d.setVisible(true);
        
        d = new DockableElement("1-3");
        d.setTitleText("1-3");
        control.addDockable(d);
        d.setVisible(true);
    }
    
    public void TestC() {
        StackDockStation s = new StackDockStation();
        s.asDockable().getComponent().setBounds(30, 30, 300, 300);
        
        control.addStation((CStation<?>) s);
        DockableElement d = new DockableElement("hola");
        s.drag((Dockable) d);
        
        
    }
    
    public void testD() {
        
        LocationNode root = new LocationNode();
        root.childrenOrientation = ChildrenOrientation.HORIZONTAL;
        
        LocationNode n1 = new LocationNode();
        n1.dock = new DockableElement("n1");
        n1.size = 0.7f;
        n1.location = Location.LEFT;
        
        LocationNode n2 = new LocationNode();
        n2.dock = new DockableElement("n2");
        n2.size = 0.3f;
        n2.location = Location.RIGHT;
        
        LocationNode n3 = new LocationNode();
        n3.dock = new DockableElement("n3");
        n3.size = 0.4f;
        n3.location = Location.RIGHT;
        
        LocationNode n1p = new LocationNode();
        n1p.location = Location.LEFT;
        n1p.size = 0.6f;
        n1p.childrenOrientation = ChildrenOrientation.HORIZONTAL;
        
        n1p.firstChild = n1;
        n1p.secondChild = n2;
        
        root.firstChild = n1p;
        n1p.parent = root;
        
        root.secondChild = n3;
        n3.parent = root;
        
        n1.parent = n1p;
        n2.parent = n1p;
        
        CGrid g = createGrid(control, root);
        control.getContentArea().deploy(g);
        
    }
    
    public void testE() {
        LocationNode r, n1, n2, n3, n2p;
        
        r = new LocationNode(ChildrenOrientation.HORIZONTAL, null, 0);
        n1 = new LocationNode(new DockableElement("n1"), 0.7f, Location.LEFT);
        n2 = new LocationNode(new DockableElement("n2"), 0.2f, Location.TOP);
        n3 = new LocationNode(new DockableElement("n3"), 0.8f, Location.BOTTOM);
        n2p = new LocationNode(ChildrenOrientation.VERTICAL, Location.LEFT, 0.3f);
        
        r.firstChild = n1;
        n1.parent = r;
        r.secondChild = n2p;
        n2p.parent = r;
        
        n2p.firstChild = n2;
        n2.parent = n2p;
        
        n2p.secondChild = n3;
        n3.parent = n2p;
        
        CGrid g = createGrid(control, r);
        control.getContentArea().deploy(g);
    }
    
    public void testF() {
        LocationNode r, n1, n2, n3, n4,n5,n6,n7,n8,n9,n10,n11;
        r = new LocationNode(ChildrenOrientation.VERTICAL, null, 0);
        n11 = new LocationNode(ChildrenOrientation.VERTICAL, .5f, Location.BOTTOM);
        n10 = new LocationNode(ChildrenOrientation.HORIZONTAL, .4f, Location.BOTTOM);
        n9 = new LocationNode(new DockableElement("9"), .4f, Location.LEFT);
        n8 = new LocationNode(new DockableElement("8"), .6f, Location.RIGHT);
        n7 = new LocationNode(ChildrenOrientation.HORIZONTAL, .6f, Location.TOP);
        n6 = new LocationNode(new DockableElement("6"), .1f, Location.RIGHT);
        n5 = new LocationNode(ChildrenOrientation.HORIZONTAL, .9f, Location.LEFT);
        n4 = new LocationNode(new DockableElement("4"), .1f, Location.LEFT);
        n3 = new LocationNode(ChildrenOrientation.VERTICAL, .9f, Location.RIGHT);
        n2 = new LocationNode(new DockableElement("2"), .4f, Location.BOTTOM);
        n1 = new LocationNode(new DockableElement("1"), .6f, Location.TOP);
        
        r.secondChild = n11;
        n11.parent = r;
        n11.firstChild = n7; n7.parent = n11;
        n11.secondChild = n10; n10.parent = n11;
        n10.firstChild = n9; n9.parent = n10;
        n10.secondChild = n8; n8.parent = n10;
        n7.firstChild = n5; n5.parent = n7;
        n7.secondChild = n6; n6.parent = n7;
        n5.firstChild = n4; n4.parent = n5;
        n5.secondChild = n3; n3.parent = n5;
        n3.firstChild = n1; n1.parent = n3;
        n3.secondChild = n2; n2.parent = n3;
        
        CGrid g = createGrid(control, r);
        control.getContentArea().deploy(g);
    }
    
    public void printLocation(CDockable d) {
        CLocation l = d.getBaseLocation();
        
        System.out.println("/'''''''''''''''''''");
        System.out.println("dockable         > "+d);
        System.out.println("d.extmode        > "+d.getExtendedMode());
        System.out.println("d.extmode.id     > "+d.getExtendedMode().getModeIdentifier());
        System.out.println("cloc             > "+l);
        System.out.println("cloc.class       > "+l.getClass());
        System.out.println("cloc.mode.id     > "+l.findMode().getModeIdentifier());
        System.out.println("cloc.root        > "+l.findRoot());
        System.out.println("cloc.prop        > "+l.findProperty());
        System.out.println("cloc.prop.class  > "+l.findProperty().getClass());
        System.out.println("cloc.parent      > "+l.getParent());
        System.out.println("\\..................");
        
        
        XElement x = new XElement("x");
        l.findProperty().store(x);
        System.out.println(x);
    }
    
    public void addSingleDockable(DefaultSingleCDockable dockable, String groupId) {

        if (!singleDockables.containsKey(groupId))
            singleDockables.put(groupId, new ArrayList());

        singleDockables.get(groupId).add(dockable);
        control.addDockable(dockable);
        
    }
    
    public void addSingleDockable(DefaultMultipleCDockable dockable, String groupId) {

        if (!multiDockables.containsKey(groupId))
            multiDockables.put(groupId, new ArrayList());

        multiDockables.get(groupId).add(dockable);
        control.addDockable(dockable);
        
    }
        
    public static void main(String[] args) {
        Test51 t = new Test51();
        t.init();
        //t.testF();
        //t.TestC();
    }
    
    public class LocationTree {
        public LocationNode root;
        
        public LocationTree(){
        } 
    }
    
    public static void printNode(LocationNode node) {
        printNode(node, "");
    }

    public static void printNode(LocationNode node, String indentation) {

        if (node != null) {
            System.out.println(indentation + node);

            printNode(node.firstChild, indentation + "  ");
            printNode(node.secondChild, indentation + "  ");
        }
    }
    
    public enum ChildrenOrientation {HORIZONTAL, VERTICAL};
    
    public enum Location {TOP, BOTTOM, LEFT, RIGHT};
    
    public class LocationNode {

        public long id;
        public float size;
        public Location location;
        DockableElement dock = null;
        public String type;
        
        public LocationNode parent = null;
        public ChildrenOrientation childrenOrientation = null;
        public LocationNode firstChild = null; //left or top
        public LocationNode secondChild = null;//right or bottom
        
        LocationNode() {}
        
        LocationNode(DockableElement d, float s, Location l) {
            dock = d;
            size = s;
            location = l;
        }

        LocationNode(ChildrenOrientation o, Location l, float s) {
            childrenOrientation = o;
            location = l;
            size = s;
        }
        
        LocationNode(ChildrenOrientation o, float s, Location l) {
            childrenOrientation = o;
            location = l;
            size = s;
        }
        
        public void preOrderPrint(LocationNode n) {
            if (n != null) {
                System.out.println(n);
                preOrderPrint(n.firstChild);
                preOrderPrint(n.secondChild);
            }
        }
        
        
        public boolean join(LocationNode otherNode) {
            
            if (this == null || otherNode == null)
                return false;
            
            // if roots are not the same can't be joined
            if (this.id != otherNode.id)
                return false;
            
            LocationNode thisFirstChild = this.firstChild;
            LocationNode thisSecondChild = this.secondChild;
            LocationNode otherFirstChild = otherNode.firstChild;
            LocationNode otherSecondChild = otherNode.secondChild;
            
//            while () {
//                if ()
//            }
            return false;
        }

        @Override
        public String toString() {
            return "[ "+id+" "+location+" "+size+" "+childrenOrientation+" "
                    + ((firstChild!=null && secondChild!=null)?"leaf":"node") + " " + dock +"]";
        }

        @Override
        public boolean equals(Object obj) {
            return this.id == ((LocationNode) obj).id;
        }

    }
}
