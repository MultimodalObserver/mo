package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.event.CDockableLocationEvent;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.util.DirectWindowProvider;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.core.ui.menubar.IMenuBarItemProvider;
import static mo.core.utils.Utils.getBaseFolder;
import mo.filemanagement.FilesPane;

@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.core.ui.menubar.IMenuBarItemProvider"
            )
        }
)
public class DockablesRegistry implements IMenuBarItemProvider {

    private static final Logger LOGGER = Logger.getLogger(DockablesRegistry.class.getName());

    private static DockablesRegistry registry;

    private final HashMap<String, List<DockableElement>> dockables; //<directory, list>

    private final CControl control;

    private final JMenu windowMenu = new JMenu("Window");

    public DockablesRegistry() {
        registry = this;
        dockables = new HashMap<>();

        control = new CControl();

        JMenuItem test = new JMenuItem("test");
        windowMenu.add(test);

        test.addActionListener((ActionEvent e) -> {
            //System.out.println("saving...");
            saveDockables();
        });
    }

    public synchronized static DockablesRegistry getInstance() {
        if (registry == null) {
            registry = new DockablesRegistry();
        }
        return registry;
    }

    public void addAppWideDockable(DockableElement dockable) {
        addDockableInProjectGroup(null, dockable);
    }

    public void addDockableInProjectGroup(String projectPath, DockableElement dockable) {
        addDockable(projectPath, dockable);
    }

    private void addDockable(String group, DockableElement dockable) {
        addDockableToHashMap(group, dockable);
        addDockableToControlAndSetVisible(dockable);

        JMenu parentMenu;
        if (group == null) {
            parentMenu = windowMenu;
        } else {
            parentMenu = getOrCreateMenuItemForGroup(group);
        }
        setupMenuItemForAppDockable(parentMenu, dockable);
    }

    private void addDockableToHashMap(String group, DockableElement d) {
        if (!dockables.containsKey(group)) {
            dockables.put(group, new ArrayList<>());
        }
        dockables.get(group).add(d);
    }

    private void addDockableToControlAndSetVisible(DockableElement d) {
        control.addDockable(d);
        d.setVisible(true);
    }

    private void setupMenuItemForAppDockable(JMenu parentMenu, DockableElement d) {

        DockableCheckBoxMenuItem menuItem = new DockableCheckBoxMenuItem();
        menuItem.setText(d.getTitleText());
        menuItem.setDockable(d);
        d.addVetoClosingListener(new CVetoClosingListener() {
            @Override
            public void closing(CVetoClosingEvent cvce) {
            }

            @Override
            public void closed(CVetoClosingEvent cvce) {
                menuItem.setSelected(false);
            }
        });
        d.addCDockableLocationListener((CDockableLocationEvent cdle) -> {
            CDockable cd = cdle.getDockable();
            if (!cd.isVisible()) {
                menuItem.setSelected(false);
                d.setBackupLocation(cdle.getOldLocation());
            }
        });
        menuItem.setState(true);

        parentMenu.add(menuItem);
        menuItem.setVisible(true);
        //System.out.println("P>" + parentMenu + "\nS>" + menuItem);
        menuItem.addItemListener(this::checkBoxMenuItemStateChanged);
    }

    private JMenu getOrCreateMenuItemForGroup(String group) {

        for (Component c : windowMenu.getMenuComponents()) {
            if ((c.getName() != null) && c.getName().equals(group) && (c instanceof JMenu)) {
                return (JMenu) c;
            }
        }

        File folder = new File(group);
        JMenu groupMenu = new JMenu(folder.getName());
        groupMenu.setName(group);
        windowMenu.add(groupMenu);
        System.out.println(groupMenu);
        return groupMenu;
    }

    private void checkBoxMenuItemStateChanged(ItemEvent event) {

        DockableCheckBoxMenuItem item = (DockableCheckBoxMenuItem) event.getItem();
        DockableElement dockable = item.getDockable();

        if (event.getStateChange() == ItemEvent.DESELECTED) {
            dockable.saveBackupLocation();
            dockable.setVisible(false);
        } else {
            dockable.restoreBackupLocation();
            dockable.setVisible(true);
        }
    }

    public void saveDockables() {
        dockables.keySet().stream().forEach((dir) -> {
            if (dir == null) {
                storeDockables(getBaseFolder(), dockables.get(null));
            } else {
                storeDockables(dir, dockables.get(dir));
            }
        });
    }

    private void storeDockables(String dir, List<DockableElement> dockables) {
        File folder = new File(dir);
        File xmlFile = new File(folder, "dockables.xml");

        if (filesAreValid(folder, xmlFile)) {

            XElement xmlContent = new XElement("dockables");
            for (DockableElement dockable : dockables) {

                if (dockable instanceof StorableDockable) {

                    StorableDockable sd = (StorableDockable) dockable;

                    XElement dock = new XElement("dockable");
                    dock.addElement(dockable.getLocationXML());
                    dock.addBoolean("isVisible", dockable.isVisible());
                    dock.addElement("id").setString(dockable.getId());

                    XElement group = new XElement("group");
                    if (!dir.equals(getBaseFolder())) {
                        group.setString(dir);
                        dock.addElement(group);
                    }

                    XElement data = new XElement("data");
                    data.addString("class", dockable.getClass().getTypeName());

                    Path docksRoot = Paths.get(dir);
                    Path dockFilePath = Paths.get(sd.dockableToFile().toURI());
                    Path relative = docksRoot.relativize(dockFilePath);
                    data.setString(relative.toString());

                    dock.addElement(data);

                    xmlContent.addElement(dock);
                }
            }
            writeXmlToFile(xmlContent, xmlFile);
        }
    }

    private boolean filesAreValid(File folder, File file) {
        if (folder.isDirectory()) {
            try {
                createFileIfNotExists(file);
                return true;
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    private void createFileIfNotExists(File file) throws IOException {
        if (!file.isFile() && !file.createNewFile()) {
            LOGGER.log(Level.WARNING, null, "Can't create file <" + file + ">");
        }
    }

    private void writeXmlToFile(XElement xml, File file) {
        try (OutputStream os = new FileOutputStream(file)) {
            XIO.writeUTF(xml, os);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void loadDockablesFromFile(File file) {
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                XElement root = XIO.readUTF(input);
                XElement[] docks = root.getElements("dockable");

                DockablesTreeRecreator treeRecreator = DockablesTreeRecreator.getInstance(control);

                for (XElement dock : docks) {
                    //System.out.println(dock);
                    XElement data = dock.getElement("data");
                    String className = data.getAttribute("class").getString();

                    DockableElement element = createDockableInstance(file, dock, className);
                    if (element != null) {

                        XElement location = dock.getElement("location");
                        XElement property = location.getElement("property");
                        XElement leaf = property.getElement("leaf");

                        if (leaf == null) {
                            control.addDockable(element);
                            element.setLocationFromXml(control, location);
                            element.setVisible(true);
                        } else {
                            treeRecreator.addDockable(element, property);
                        }

                        XElement group = dock.getElement("group");
                        if (group == null) {
                            addDockableInProjectGroup(null, element);
                        } else {
                            addDockableInProjectGroup(group.getString(), element);
                        }

                        boolean isVisible = dock.getAttribute("isVisible").getBoolean();
                        if (!isVisible) {
                            element.setVisible(false);
                        }
                    }
                }

                List<DockablesTreeRecreator.LocationNode> trees = treeRecreator.joinTrees();
                treeRecreator.createTrees(trees);
                
                //System.out.println(">>>"+trees);

            } catch (IOException | SecurityException | IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("No dockables config found");
        }
    }

    private DockableElement createDockableInstance(File file, XElement dockableXData, String className) {
        DockableElement sd = null;
        try {
            Class<?> clazz = Class.forName(className);
            Object o = clazz.newInstance();
            String dataStr = dockableXData.getElement("data").getString();
            Method method = clazz.getDeclaredMethod("dockableFromFile", File.class);

            sd = (DockableElement) method.invoke(o, new File(file.getParentFile(), dataStr));
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return sd;
    }

    public void setJFrame(JFrame frame) {
        getInstance();
        control.setRootWindow(new DirectWindowProvider(frame));
    }

    public JFrame getMainFrame() {
        return (JFrame) control.getRootWindow().searchWindow();
    }

    public CControl getControl() {
        return this.control;
    }

    @Override
    public JMenuItem getItem() {
        return windowMenu;
    }

    @Override
    public int getRelativePosition() {
        return AFTER;
    }

    @Override
    public String getRelativeTo() {
        return "file";
    }

    public class DockableCheckBoxMenuItem extends JCheckBoxMenuItem {

        DockableElement dockable;

        public void setDockable(DockableElement dockable) {
            this.dockable = dockable;
        }

        public DockableElement getDockable() {
            return this.dockable;
        }
    }
}
