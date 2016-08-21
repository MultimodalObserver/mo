package mo.filemanagement;

import java.io.File;
import java.util.TreeMap;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.core.ui.dockables.DockablesRegistry;

public class FileRegistry {
    
    private static FileRegistry fileRegistry;
    
    private final TreeMap<String,String> openedFiles; //<path, name>
    
    private FilesPane filesPane;
    
    public FileRegistry() {
        openedFiles = new TreeMap<>();
        Plugin filesPanePlugin = PluginRegistry.getInstance().getPlugin("mo.filemanagement.FilesPane");
        if (filesPanePlugin != null) {
            filesPane = (FilesPane) filesPanePlugin.getInstance();
            DockablesRegistry.getInstance().addAppWideDockable(filesPane);
        }
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
        
        filesPane.addFile(new File(project.getFolder().getAbsolutePath()));
    }

    public FilesPane getFilesPane() {
        return filesPane;
    }

    public void setFilesPane(FilesPane filesPane) {
        this.filesPane = filesPane;
    }
}
