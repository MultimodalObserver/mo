package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.event.CDockableLocationEvent;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.util.DirectWindowProvider;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private final  JMenu windowMenu = new JMenu("Window");

    private final List<JMenuItem> dockablesGroupedMenus;

    public DockablesRegistry() {
        registry = this;
        dockables = new HashMap<>();

        control = new CControl();
        dockablesGroupedMenus = new ArrayList<>();

        JMenuItem test = new JMenuItem("test");
        windowMenu.add(test);
        test.addActionListener((ActionEvent e) -> {
            System.out.println("saving...");
            saveDockables();
        });
    }

    public static DockablesRegistry getInstance() {
        if (registry == null) {
            registry = new DockablesRegistry();
        }
        return registry;
    }

    public void addAppWideDockable(DockableElement dockable) {
        addDockableInProjectGroup(null, dockable);
    }

    public void addDockableInProjectGroup(String projectPath, DockableElement dockable) {
        if (!dockables.containsKey(projectPath)) {
            dockables.put(projectPath, new ArrayList<>());
        }

        dockables.get(projectPath).add(dockable);
        addDockableElement(dockable);
    }

    private void addDockableElement(DockableElement dockable) {
        control.addDockable(dockable);
        dockable.setVisible(true);

        DockableCheckBoxMenuItem dockableCheckItem = new DockableCheckBoxMenuItem();
        dockableCheckItem.setText(dockable.getTitleText());
        dockableCheckItem.setDockable(dockable);
        dockable.addVetoClosingListener(new CVetoClosingListener() {
            @Override
            public void closing(CVetoClosingEvent cvce) {
            }

            @Override
            public void closed(CVetoClosingEvent cvce) {
                dockableCheckItem.setSelected(false);
            }
        });
        dockable.addCDockableLocationListener((CDockableLocationEvent cdle) -> {
            CDockable d = cdle.getDockable();

            if (!d.isVisible()) {
                dockableCheckItem.setSelected(false);
                dockable.setBackupLocation(cdle.getOldLocation());
            }
        });
        dockableCheckItem.setState(true);
        windowMenu.add(dockableCheckItem);
        dockableCheckItem.addItemListener(this::checkBoxMenuItemStateChanged);
    }

    private void checkBoxMenuItemStateChanged(ItemEvent event) {

        DockableCheckBoxMenuItem item = (DockableCheckBoxMenuItem) event.getItem();
        DockableElement dockable = item.getDockable();

        if (event.getStateChange() == ItemEvent.DESELECTED) {
            dockable.setBackupLocation(dockable.getBaseLocation());
            dockable.setVisible(false);
        } else {
            dockable.setLocation(dockable.getBackupLocation());
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
                System.out.println(dockable);
                if (dockable instanceof StorableDockable) {
                
                    StorableDockable sd = (StorableDockable) dockable;
                    
                    XElement dock = new XElement("dockable");
                    dock.addElement(dockable.getLocationXML());
                    dock.addBoolean("isVisible", dockable.isVisible());
                    
                    XElement group = new XElement("group");
                    if ( !dir.equals(getBaseFolder())) {
                        group.setString(dir);
                        dock.addElement(group);
                    }
                    
                    XElement data = new XElement("data");
                    data.addString("class", dockable.getClass().getCanonicalName());
                    data.setString(sd.toFileContent());
                    
                    
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
            LOGGER.log(Level.WARNING, null,"Can't create dockables file <" + file + ">");
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
        try (InputStream input = new FileInputStream(file)) {
            XElement root = XIO.readUTF(input);
            XElement[] docks = root.getElements("dockable");
            for (XElement dock : docks) {
                System.out.println(dock);
                XElement data = dock.getElement("data");
                String className = data.getAttribute("class").getString();
                Class<?> clazz = Class.forName(className);
                Plugin p = PluginRegistry.getInstance().getPlugin(className);
                
                System.out.println(p);
                Method method = clazz.getDeclaredMethod("dockableFromFile", String.class);
                
                String dataStr = dock.getElement("data").getString();
                
                DockableElement sd = 
                        (DockableElement) method.invoke(p.getInstance(), dataStr);
                
                String group = dock.getElement("group").getString();
                
                addDockableInProjectGroup(group, sd);
            }
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
                IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
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
    
    public static void main(String[] args) {
        getInstance().loadDockablesFromFile(new File(getBaseFolder(), "dockables.xml"));
        System.exit(0);
    }
}
