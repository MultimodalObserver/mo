package mo.filemanagement;

import java.io.File;
import java.util.TreeMap;
import mo.core.ui.frames.DockablesRegistry;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class FileRegistry {
    
    private static FileRegistry fileRegistry;
    
    private final TreeMap<String,String> openedFiles; //<path, name>
    
    private FilesPane filesPane;

    public FileRegistry() {
        openedFiles = new TreeMap<>();
        filesPane = new FilesPane();
    }

    public void addOpenedFile(File p) {
        
    }
    
    public TreeMap<String, String> getOpenedFiles() {
        return openedFiles;
    }
    
    public static FileRegistry getInstance() {
        if (fileRegistry == null) {
            fileRegistry = new FileRegistry();
        }
        return fileRegistry;
    }

    public void addOpenedProject(Project project) {
        openedFiles.put(
                project.getFolder().getAbsolutePath(),
                project.getFolder().getName());
        filesPane.getFilesTreeModel()
                .addFile(new File(project.getFolder().getAbsolutePath()));
        //filesPane.refreshFiles();
        
    }

    public FilesPane getFilesPane() {
        return filesPane;
    }

    public void setFilesPane(FilesPane filesPane) {
        this.filesPane = filesPane;
    }
}
