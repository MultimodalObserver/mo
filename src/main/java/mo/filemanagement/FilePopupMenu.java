package mo.filemanagement;

import java.io.File;
import javax.swing.JPopupMenu;

public class FilePopupMenu extends JPopupMenu {
    File file;
    
    public void setFile(File file) {
        this.file = file;
    }
    
    public File getFile() {
        return file;
    }
}
