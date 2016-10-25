package mo.core.ui.menubar;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import mo.core.I18n;

import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import static mo.core.ui.menubar.IMenuBarItemProvider.AFTER;
import static mo.core.ui.menubar.IMenuBarItemProvider.BEFORE;
import static mo.core.ui.menubar.IMenuBarItemProvider.UNDER;

public class MenuBar extends JMenuBar {
    
    private static MenuBar mb;
    
    private final List<JMenuItem> menuItems;
    
    private I18n inter;
    
    private MenuBar() {
        super();
        menuItems = new ArrayList<>();
        inter = new I18n(MenuBar.class);
    }
    
    private void init() {
        JMenu m = new JMenu();
        m.setName("file");
        m.setText(inter.s("MenuBar.fileMenu"));
        addItem(m, 0, null);
        
        //m = new JMenu("Window");
        //addItem(m, 1, null);
        
        m = new JMenu();
        m.setName("plugins");
        m.setText(inter.s("MenuBar.pluginsMenu"));
        addItem(m, 2, null);
        
        m = new JMenu();
        m.setName("options");
        m.setText(inter.s("MenuBar.optionsMenu"));
        addItem(m, 3, null);
    }
    
    public void setup() {
    
        List<Plugin> plugins = 
                PluginRegistry
                        .getInstance()
                        .getPluginsFor("mo.core.ui.menubar.IMenuBarItemProvider");
        
        plugins.stream().forEach((plugin) -> {
            
            IMenuBarItemProvider i;
            i = (IMenuBarItemProvider) plugin.getInstance();
            addItem(i.getItem(),
                    i.getRelativePosition(),
                    i.getRelativeTo().toLowerCase());
            
        });
        
    }
    
    public static MenuBar getInstance() {
        if (mb == null) {
            mb = new MenuBar();
            mb.init();
            mb.setup();
        }
        return mb;
    }
    
    public JMenuItem getNamedItem(String name) {

        for (JMenuItem menuItem : menuItems) {
            if (menuItem.getName().compareTo(name) == 0) {
                //System.out.println(menuItem.getName());
                return menuItem;
            }
        }
        
        return null;
    }
    
    private void addItem(JMenuItem item, int position, String relativeTo) {

        if (item.getName() == null || item.getName().isEmpty())
            if (item.getText() != null && !item.getText().isEmpty())
                item.setName(item.getText().toLowerCase());
        
        JMenuItem relative = 
                relativeTo != null ? getNamedItem(relativeTo) : null;
        
        
        if (relative == null) {
            mb.add(item);
        } else {
            switch (position){
                case UNDER: {
                    //only JMenu can have items
                    if (relative instanceof JMenu) {
                        relative.add(item);
                    } else {
                        System.out.println("Can't add JMenu");
                    }
                    break;
                }
                case BEFORE: {
                    int i = relative.getParent().getComponentZOrder(relative);
                    relative.getParent().add(item, i);
                    break;
                }
                case AFTER: {
                    int i = relative.getParent().getComponentZOrder(relative);
                    relative.getParent().add(item, i + 1);
                    break;
                }
                default: {
                    if (position >= -1)
                        relative.add(item, position);
                    else
                        relative.add(item);
                    break;
                }
            }
        }
        menuItems.add(item);
    }
    
    @Override
    public String toString() {
        String result = "MenuBar [\n";
        for (JMenuItem menuItem : menuItems) {
            result += " "+menuItem + "\n";
        }
        result = result.substring(0,result.length()-1);
        result+= "\n]";
        return result;
    }
    
    //Test
    public static void main(String[] args) {

        JFrame f = new JFrame("asd");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setSize(300, 300);

        //MenuBar m = new MenuBar1();
        System.out.println(PluginRegistry.getInstance().getPlugins().size());
        for (Plugin arg : PluginRegistry.getInstance().getPlugins()) {
            System.out.println(arg);
        }
        //MenuBar.getInstance().add(m);
        f.setJMenuBar(MenuBar.getInstance());
        //f.setJMenuBar(MenuBar1.getInstance());
        f.setVisible(true);
    }
}
