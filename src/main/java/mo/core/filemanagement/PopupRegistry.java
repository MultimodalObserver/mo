package mo.core.filemanagement;

import java.awt.Component;
import java.awt.Container;
import mo.core.filemanagement.project.ProjectOptionProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import static mo.core.filemanagement.project.ProjectUtils.isProjectFolder;

public final class PopupRegistry {
    private static PopupRegistry popupRegistry;
    HashMap <String, FilePopupMenu> popups; //by extension
    private final List<JMenuItem> projectOptions;
    private final FilePopupMenu projectPopup;
    private FilePopupMenu folderPopup;
    
    private PopupRegistry() {
        popups = new HashMap<>();
        projectOptions = new ArrayList<>();
        projectPopup = new FilePopupMenu();
        
        for (Plugin p : PluginRegistry.getInstance().getPluginsFor("mo.core.filemanagement.PopupOptionProvider")) {
            addPopupOptionFor(((PopupOptionProvider) p.getInstance()).getPopupItem(), null);
        }
        
        for (Plugin p : PluginRegistry.getInstance().getPluginsFor("mo.core.filemanagement.project.ProjectOptionProvider")) {
            addPopupOptionForProjects(((ProjectOptionProvider) p.getInstance()).getOption());
        }
    }
    
    public void addPopupOptionForFolders(PopupItem item) {
        
    }
    
    public void addPopupOptionForProjects(JMenuItem item) {
        projectOptions.add(item);
        projectPopup.add(item);
        printJMenuItem(item, "");
        
    }
    
    private void printJMenuItem(JMenuItem i, String indent) {
        System.out.println(indent + i);
        for (MenuElement subElement : i.getSubElements()) {
            //printJMenuItem((JMenuItem) subElement, indent+" ");
            
        }
    }
    
    public void addPopupOptionFor(JMenuItem itemToAdd, String fileExtension) {
        if (!popups.containsKey(fileExtension))
            popups.put(fileExtension, new FilePopupMenu());
        
        //FilePopupMenu popup = popups.get(fileExtension);
        //JMenuItem item = itemToAdd.getItem();
        //int position = itemToAdd.getPosition();
        
        //popup.add(itemToAdd);
    }
    
    private void putPropertyInChildrenComponents(MenuElement[] menus, Object key, Object value) {
        for (MenuElement menu : menus) {
            if (menu instanceof JComponent) {
                System.out.println("property in "+menu);
                ((JMenuItem) menu).putClientProperty(key, value);
                putPropertyInChildrenComponents(menu.getSubElements(), key, value);
            }
        }
    }
    
    private void printTree(Component c, String indent) {
        System.out.println(indent + c);
        if (c instanceof Container) {
            for (Component component : ((Container) c).getComponents()) {
                printTree(component, indent+" ");
            }
        }
    }
    
    private void printSubElements(Object o, String indent) {
        if (o instanceof MenuElement) {
            System.out.println(indent + o);
            MenuElement e = (MenuElement) o;
            for (MenuElement subElement : e.getSubElements()) {
                printSubElements(subElement, indent+" ");
            }
        }
    }
    
    private void setFileProperty(Object object, Object key, Object value) {
        
        if (object instanceof JComponent) {
            ((JComponent) object).putClientProperty(key, value);
        }
        
        if (object instanceof MenuElement) {
            MenuElement e = (MenuElement) object;
            for (MenuElement subElement : e.getSubElements()) {
                setFileProperty(subElement, key, value);
            }
        }
    }
    
    FilePopupMenu getPopupFor(File file) {
        System.out.println("file: "+file);
        if (file.isDirectory()) {
            if (isProjectFolder(file)) {
                //putPropertyInChildrenComponents( projectPopup.getSubElements(), "file", file);
                //projectPopup.get
                projectPopup.setFile(file);
                System.out.println(projectPopup);
                setFileProperty(projectPopup, "file", file);
                return projectPopup;
            } else {
                return folderPopup;
            }
        } else {
            String extension = null;
        
            if (file.getName().contains(".")) {
                extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
            }
            
            return popups.get(extension);
        }
    }
    
    public static PopupRegistry getInstance() {
        if (popupRegistry == null) {
            popupRegistry = new PopupRegistry();
        }
        
        return popupRegistry;
    }
}
