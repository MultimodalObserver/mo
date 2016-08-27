package mo.filemanagement;

import mo.filemanagement.project.Project;
import java.io.File;
import java.util.TreeMap;
import mo.core.ui.dockables.DockablesRegistry;

public class FileRegistry {
    
    private static FileRegistry fileRegistry;
    
    private final TreeMap<String,String> openedFiles; //<path, name>
    
    private FilesPane filesPane;
    
    private FileRegistry() {
        openedFiles = new TreeMap<>();
    }
    
    public TreeMap<String, String> getOpenedFiles() {
        return openedFiles;
    }
    
    public synchronized static FileRegistry getInstance() {
        if (fileRegistry == null) {
            fileRegistry = new FileRegistry();
        }
        return fileRegistry;
    }

    public void addOpenedProject(Project project) {
        
        File f = project.getFolder();
                
        openedFiles.put(
                f.getAbsolutePath(),
                f.getName());
        System.out.println(project);
        System.out.println(project.getFolder());
        System.out.println(filesPane);
        filesPane.addFile(new File(f.getAbsolutePath()));

        DockablesRegistry.getInstance().loadDockablesFromFile(new File(f, "dockables.xml"));
    }

    public FilesPane getFilesPane() {
        return filesPane;
    }

    public void setFilesPane(FilesPane filesPane) {
        System.out.println("seted "+filesPane);
        this.filesPane = filesPane;
    }
}
