package mo.filemanagement;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import static mo.filemanagement.ProjectUtils.isProjectFolder;

public final class PopupRegistry {
    private static PopupRegistry popupRegistry;
    HashMap <String, FilePopupMenu> popups; //by extension
    private final List<JMenuItem> projectOptions;
    private final FilePopupMenu projectPopup;
    private JPopupMenu folderPopup;
    
    private PopupRegistry() {
        popups = new HashMap<>();
        projectOptions = new ArrayList<>();
        projectPopup = new FilePopupMenu();
        
        for (Plugin p : PluginRegistry.getInstance().getPluginsFor("mo.filemanagement.PopupOptionProvider")) {
            addPopupOptionFor(((PopupOptionProvider) p.getInstance()).getPopupItem(), null);
        }
        
        for (Plugin p : PluginRegistry.getInstance().getPluginsFor("mo.filemanagement.ProjectOptionProvider")) {
            addPopupOptionForProjects(((ProjectOptionProvider) p.getInstance()).getOption());
        }
    }
    
    public void addPopupOptionForFolders(PopupItem item) {
        
    }
    
    public void addPopupOptionForProjects(JMenuItem item) {
        projectOptions.add(item);
        projectPopup.add(item);
    }
    
    public void addPopupOptionFor(JMenuItem itemToAdd, String fileExtension) {
        if (!popups.containsKey(fileExtension))
            popups.put(fileExtension, new FilePopupMenu());
        
        //FilePopupMenu popup = popups.get(fileExtension);
        //JMenuItem item = itemToAdd.getItem();
        //int position = itemToAdd.getPosition();
        
        //popup.add(itemToAdd);
    }
    
    JPopupMenu getPopupFor(File file) {
        if (file.isDirectory()) {
            if (isProjectFolder(file)) {
                projectPopup.setFile(file);
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
