package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.event.CDockableLocationEvent;
import bibliothek.gui.dock.common.event.CDockableLocationListener;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.util.DirectWindowProvider;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    private final Logger LOGGER = Logger.getLogger(DockablesRegistry.class.getName());

    private static DockablesRegistry registry;

    private final CControl control;

    public JMenu windowMenu = new JMenu("Window");

    // mantiene todos los dockables de la app
    private final HashMap<String, List<DockableElement>> dockables;

    public List<JMenuItem> dockablesGroupedMenus;

    public DockablesRegistry() {
        registry = this;
        dockables = new HashMap<>();

        control = new CControl();
        dockablesGroupedMenus = new ArrayList<>();

        JMenuItem test = new JMenuItem("test");
        windowMenu.add(test);
        test.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("saving...");
                saveDockables();
            }
        });
    }

    public static DockablesRegistry getInstance() {
        if (registry == null) {
            registry = new DockablesRegistry();
        }
        return registry;
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
        dockable.addCDockableLocationListener(new CDockableLocationListener() {
            @Override
            public void changed(CDockableLocationEvent cdle) {

                CDockable d = cdle.getDockable();

                if (!d.isVisible()) {
                    dockableCheckItem.setSelected(false);
                    dockable.setBackupLocation(cdle.getOldLocation());
                }

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
        if (folder.isDirectory()) {
            File xml = new File(folder, "dockables.xml");
            try {
                if (!xml.isFile()) {
                    if (!xml.createNewFile()) {
                        LOGGER.log(Level.WARNING, null, "Can't create dockables file");
                        return;
                    }
                }

                XElement dockablesElement = new XElement("dockables");
                for (DockableElement d : dockables) {
                    XElement dock = new XElement("dockable");
                    dock.addElement(LocationUtils.getLocationXML(d));
                    dockablesElement.addElement(dock);
                }

                OutputStream os = null;
                try {
                    os = new FileOutputStream(xml);
                    XIO.writeUTF(dockablesElement, os);
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(DockablesRegistry.class.getName()).log(Level.SEVERE, null, ex);
            }
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

}
