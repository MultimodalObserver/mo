package mo.core.ui.dockables;

import bibliothek.gui.dock.common.*;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.layout.DockableProperty;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class Test5 {

    private final static Logger LOGGER = Logger.getLogger(Test5.class.getName());

    JFrame frame;
    CControl control;

    File file = new File("elements.xml");
    private HashMap<String, ArrayList<DefaultSingleCDockable>> singleDockables;

    public void init() {

        frame = new JFrame("Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        control = new CControl(frame);

        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("xml");

        JMenuItem item = new JMenuItem("imprime");
        item.addActionListener((ActionEvent e) -> {
            for (int i = 0; i < control.getCDockableCount(); i++) {
                CDockable d = control.getCDockable(i);
                System.out.println(((DockableElement) d).getLocationXML());
            }
        });
        bar.add(menu);
        menu.add(item);

        item = new JMenuItem("save");
        item.addActionListener((ActionEvent e) -> {
            XElement xe = new XElement("dockables");
            XElement root = new XElement("root");
            System.out.println(control.getCDockableCount() + " to save");
            for (int i = 0; i < control.getCDockableCount(); i++) {
                if (control.getCDockable(i) instanceof DockableElement) {

                    DockableElement d = (DockableElement) control.getCDockable(i);
                    System.out.println(d.getId());
                    XElement dock = new XElement("dockable");
                    dock.addElement("id").setString(d.getId());
                    dock.addElement(d.getLocationXML());
                    xe.addElement(dock);
                }
            }
            root.addElement(xe);

            try (OutputStream os = new FileOutputStream(file)) {
                XIO.writeUTF(root, os);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });
        menu.add(item);

        item = new JMenuItem("load");
        item.addActionListener((ActionEvent e) -> {
            try (InputStream in = new FileInputStream(file)) {
                XElement xe = XIO.readUTF(in);

                XElement doc = xe.getElement("dockables");
                XElement[] docks = doc.children();

                for (XElement dock : docks) {
                    XElement property = dock.getElement("location").getElement("property");
                    if (property.getElement("leaf") == null) {

                        DockableElement de = new DockableElement(dock.getElement("id").getValue());
                        de.setTitleText(dock.getElement("id").getValue());
                        control.addDockable(de);

                        setLocationFromXml(de, dock.getElement("location"));
                        de.setVisible(true);
                    }
                }
                recreateTree(docks);

            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        });
        menu.add(item);

        JMenu menu2 = new JMenu("dockables");
        JMenuItem addDock = new JMenuItem("add dock");
        addDock.addActionListener((ActionEvent e) -> {
            int count = control.getCDockableCount();
            count++;
            DockableElement de = new DockableElement(Integer.toString(count));
            de.setTitleText(de.getId());
            control.addDockable(de);
            de.setVisible(true);
        });
        menu2.add(addDock);
        JMenuItem del = new JMenuItem("delete all");
        del.addActionListener((ActionEvent e) -> {
            while (control.getCDockableCount() > 0) {
                control.removeDockable((SingleCDockable) control.getCDockable(0));
            }
        });
        menu2.add(del);

        bar.add(menu2);

        frame.setJMenuBar(bar);
        frame.add(control.getContentArea());
        frame.setLocation(200, 200);
        frame.setSize(350, 350);
        frame.setVisible(true);
    }

    private void setLocationFromXml(DockableElement dockable, XElement xmlLocationInfo) {
        String type = xmlLocationInfo.getAttribute("type").getValue();

        if (type.equals("CExternalizedLocation")) {
            System.out.println("externalized");
            int x, y, w, h;
            XElement prop = xmlLocationInfo.getElement("property");
            x = prop.getElement("x").getInt();
            y = prop.getElement("y").getInt();
            w = prop.getElement("width").getInt();
            h = prop.getElement("height").getInt();

            if (xmlLocationInfo.getElement("mode").getValue().endsWith("maximized")) {
                System.out.println("  maximized");
                dockable.setLocation(CLocation.external(x, y, w, h));
                //TODO maximize
            } else {
                System.out.println("  no maximized");
                dockable.setLocation(CLocation.external(x, y, w, h));

            }
        } else if (type.equals("CMaximalExternalizedLocation")) {
            //never happends?
        } else if (type.equals("CStackLocation")) {
            System.out.println("  cstacklocation");
            String mode = xmlLocationInfo.getElement("mode").getValue();
            if (mode.endsWith("externalized")) {
                System.out.println("   external");
                XElement property = xmlLocationInfo.getElement("property");
                int x = property.getElement("x").getInt();
                int y = property.getElement("y").getInt();
                int w = property.getElement("width").getInt();
                int h = property.getElement("height").getInt();
                List<DockableElement> l = findWithBounds(x, y, w, h);
                System.out.println("l size: " + l.size());
                if (l.isEmpty()) {
                    System.out.println("no l");
                    dockable.setLocation(CLocation.external(x, y, w, h));

                } else {
                    System.out.println("si l : " + l.get(0).id);
                    dockable.setLocationsAside(l.get(0));
                }
            } else if (mode.endsWith("normal")) {
                System.out.println("   normal");
            }
        } else if (type.equals("CFlapIndexLocation")) {
            dockable.setLocation(CLocation.base());
            dockable.setExtendedMode(ExtendedMode.MINIMIZED);
        } else if (type.equals("TreeLocationLeaf")) {
            //delegated to tree
        } else {
            dockable.setLocation(CLocation.base());
        }
    }

    public List<DockableElement> findWithBounds(int x, int y, int w, int h) {
        ArrayList<DockableElement> result = new ArrayList<>();
        for (int i = 0; i < control.getCDockableCount(); i++) {
            boolean xtrue = false, ytrue = false, wtrue = false, htrue = false;
            DockableElement d = (DockableElement) control.getCDockable(i);
            System.out.println(d.id);
            DockableProperty p;
            if (d.getBaseLocation() != null) {
                p = d.getBaseLocation().findProperty();
            } else if (d.getAutoBaseLocation(false) != null) {
                p = d.getAutoBaseLocation(false).findProperty();
            } else if (d.getAutoBaseLocation(true) != null) {
                p = d.getAutoBaseLocation(true).findProperty();
            } else {
                continue;
            }
            XElement store = new XElement("store");
            System.out.println(p);
            p.store(store);
            for (int j = 0; j < store.getElementCount(); j++) {
                XElement current = store.getElement(j);
                try {
                    if (current.getName().equals("x")
                            && current.getInt() == x) {
                        xtrue = true;
                    } else if (current.getName().equals("y")
                            && current.getInt() == y) {
                        ytrue = true;
                    } else if (current.getName().equals("width")
                            && current.getInt() == w) {
                        wtrue = true;
                    } else if (current.getName().equals("height")
                            && current.getInt() == h) {
                        htrue = true;
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }

            if (xtrue && ytrue && wtrue && htrue) {
                result.add(d);
            }
        }
        return result;
    }

    public void recreateTree(XElement[] dockables) {

        HashMap<Long, List<LocationNode>> treesByRoot = new HashMap<>();

        
        for (XElement dockable : dockables) {
            // create tree parts for every dockable and stores them by root id
            XElement location = dockable.getElement("location");
            XElement property = location.getElement("property");
            XElement[] nodes = property.getElements("node");
            int lastNodeIndex = nodes.length -1;
            XElement lastXNode = nodes[lastNodeIndex];
            XElement leaf = property.getElement("leaf");
            if (leaf != null) {
                
                String locationStr = lastXNode.getAttribute("location").getString();

                LocationNode last = new LocationNode();
                last.docks.add(new DockableElement(dockable.getElement("id").getString()));
                last.id = leaf.getAttribute("id").getLong();
                last.location = Location.fromString(locationStr);
                last.size = lastXNode.getAttribute("size").getFloat();
                last.type = property.getAttribute("type").getString();

                LocationNode lastVisited = last;

                for (int j = lastNodeIndex; j > -1; j--) {

                    LocationNode n = new LocationNode();
                    n.id = nodes[j].getAttribute("id").getLong();

                    if (j > 0) {
                        n.location = Location.fromString(nodes[j - 1]
                                .getAttribute("location").getString());

                        n.size = nodes[j - 1].getAttribute("size").getFloat();
                    }

                    Location childLocation = lastVisited.location;

                    if (null != childLocation) {
                        switch (childLocation) {
                            case TOP:
                                n.childrenOrientation = ChildrenOrientation.VERTICAL;
                                n.firstChild = lastVisited;
                                break;
                            case BOTTOM:
                                n.childrenOrientation = ChildrenOrientation.VERTICAL;
                                n.secondChild = lastVisited;
                                break;
                            case LEFT:
                                n.childrenOrientation = ChildrenOrientation.HORIZONTAL;
                                n.firstChild = lastVisited;
                                break;
                            case RIGHT:
                                n.childrenOrientation = ChildrenOrientation.HORIZONTAL;
                                n.secondChild = lastVisited;
                                break;
                            default:
                                break;
                        }
                    }

                    lastVisited = n;
                }

                printNode(lastVisited);

                if (!treesByRoot.containsKey(lastVisited.id)) {
                    treesByRoot.put(lastVisited.id, new ArrayList<>());
                }

                treesByRoot.get(lastVisited.id).add(lastVisited);
            }
        }

        ArrayList<LocationNode> trees = new ArrayList<>();

        // for every root id, its trees are joined
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
                        } else {
                            currentMain.docks.addAll(currentNew.docks);
                            added = true;
                        }
                    }
                }
            }
            trees.add(tree);
        }
        if (trees.size() > 0) {
            printNode(trees.get(0));
            CGrid g = createGrid(control, trees.get(0));
            control.getContentArea().deploy(g);
        }
    }
    
    public CGrid createGrid(CControl control, LocationNode root) {
        CGrid grid = new CGrid(control);
        addDockableFromTree(grid, root, 0, 0, 1, 1);
        return grid;
    }

    public void addDockableFromTree(CGrid grid, LocationNode node, float x, float y, float w, float h) {

        System.out.format("(%.2f %.2f) %.2f %.2f %s%n", x, y, w, h, node);

        if (node.docks.size() > 0) {
            DockableElement[] arr = new DockableElement[node.docks.size()];
            node.docks.toArray(arr);
            System.out.println(node.docks);
            grid.add(x, y, w, h, arr);
        } else {
            float fw = w, fh = h;
            if (node.firstChild != null) {

                if (node.childrenOrientation.equals(ChildrenOrientation.HORIZONTAL)) {
                    if (node.secondChild != null) {
                        fw *= node.firstChild.size;
                    }
                } else if (node.secondChild != null) {
                    fh *= node.firstChild.size;
                }

                addDockableFromTree(grid, node.firstChild, x, y, fw, fh);
            }

            if (node.secondChild != null) {

                if (node.childrenOrientation.equals(ChildrenOrientation.HORIZONTAL)) {
                    if (node.firstChild != null) {
                        x += node.firstChild.size * w;
                        w *= node.secondChild.size;
                    }
                } else if (node.firstChild != null) {
                    y += node.firstChild.size * h;
                    h *= node.secondChild.size;
                }

                addDockableFromTree(grid, node.secondChild, x, y, w, h);
            }
        }
    }

    public void addSingleDockable(DefaultSingleCDockable dockable, String groupId) {

        if (!singleDockables.containsKey(groupId)) {
            singleDockables.put(groupId, new ArrayList());
        }

        singleDockables.get(groupId).add(dockable);
        control.addDockable(dockable);

    }

    public static void main(String[] args) {
        Test5 t = new Test5();
        t.init();
        //t.testF();
        //t.TestC();
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

    public enum ChildrenOrientation {
        HORIZONTAL, VERTICAL
    };

    public enum Location {
        TOP("TOP"), BOTTOM("BOTTOM"), LEFT("LEFT"), RIGHT("RIGHT");

        private final String val;

        private Location(String s) {
            this.val = s;
        }

        public static Location fromString(String s) {
            s = s.toUpperCase();
            for (Location value : values()) {
                if (value.toString().equals(s)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unrecognised string for location");
        }
    };

    public class LocationNode {

        public long id;
        public float size;
        public Location location;
        List<DockableElement> docks = null;
        public String type;

        public LocationNode parent = null;
        public ChildrenOrientation childrenOrientation = null;
        public LocationNode firstChild = null; //left or top
        public LocationNode secondChild = null;//right or bottom

        LocationNode() {
            docks = new ArrayList<>();
        }

        LocationNode(float s, Location l) {
            docks = new ArrayList<>();
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

        @Override
        public String toString() {
            return "[ " + id + " " + location + " " + size + " " + childrenOrientation + " "
                    + ((firstChild != null && secondChild != null) ? "leaf" : "node")
                    + " " + docks + "]";
        }
    }
}
