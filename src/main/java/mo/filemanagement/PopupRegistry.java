package mo.filemanagement;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class PopupRegistry {
    private static PopupRegistry popupRegistry;
    HashMap <String, FilePopup> popups; //by extension
    private JPopupMenu projectFolderPopup;
    private JPopupMenu folderPopup;
    
    private PopupRegistry() {
        popups = new HashMap<>();
    }
    
    public void addPopupOptionFor(PopupItem itemToAdd, String fileExtension) {
        if (!popups.containsKey(fileExtension))
            popups.put(fileExtension, new FilePopup());
        
        FilePopup popup = popups.get(fileExtension);
        JMenuItem item = itemToAdd.getItem();
        int position = itemToAdd.getPosition();
        
        popup.add(item, position);
        
        
    }
    
    JPopupMenu getPopupFor(File file) {
        if (file.isDirectory()) {
            if (isProjectFolder(file)) {
                return projectFolderPopup;
            } else {
                return folderPopup;
            }
        } else {
            String extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
            return popups.get(extension);
        }
    }
    
    public static PopupRegistry getInstance() {
        if (popupRegistry == null) {
            popupRegistry = new PopupRegistry();
        }
        
        return popupRegistry;
    }

    private boolean isProjectFolder(File file) {
        if (file.isDirectory()) {
            FilenameFilter filter = 
                    (File dir, String name) -> name.equals("moproject.xml");
            
            return file.list(filter).length == 1;
        }
        return false;
    }
}
