package mo.core.preferences;

//import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.io.File;
import java.util.HashSet;

/**
 *
 * @author Celso
 */
@XStreamAlias("preferences")
public class AppPreferencesWrapper {
    
    @XStreamAlias("projects")
    private HashSet<AppProjectPreferencesWrapper> openedProjects;
    
    @XStreamAlias("plugins")
    private HashSet<String> pluginFolders;
    
    //@XStreamAlias("")
    
    public AppPreferencesWrapper(){
    }
    
    public HashSet<AppProjectPreferencesWrapper> getOpenedProjects(){
        if (openedProjects == null)
            openedProjects = new HashSet<>();
        
        return this.openedProjects;
    }
    
    public void addOpenedProject(String projectAbsPath){
        if (openedProjects == null)
            openedProjects = new HashSet<>();
        
        openedProjects.add(new AppProjectPreferencesWrapper(projectAbsPath));
    }
    
    public void removeOpenedProject(String projectAbsPath){
        if (openedProjects == null)
            openedProjects = new HashSet<>();
        
        AppProjectPreferencesWrapper projectToRemove = null;
        
        for (AppProjectPreferencesWrapper project : openedProjects) {
            if (project.getLocation().compareTo(projectAbsPath) == 0)
                projectToRemove = project;
        }
        
        openedProjects.remove(projectToRemove);
    }

    public HashSet<String> getPluginFolders() {
        return pluginFolders;
    }

    public void setPluginFolders(HashSet<String> pluginFolders) {
        this.pluginFolders = pluginFolders;
    }

    public void addPluginFolder(String folder){
        if (pluginFolders==null)
            pluginFolders = new HashSet<>();
        
        if (!pluginFolders.contains(folder))
            pluginFolders.add(folder);
    }
    
    public static void main(String [] args){
        XStream x = new XStream();
        
        x.processAnnotations(AppPreferencesWrapper.class);
        AppPreferencesWrapper a = new AppPreferencesWrapper();
        a.addOpenedProject("ruta");
        File f = new File("");
        a.addOpenedProject(f.getAbsolutePath() );
        System.out.println(x.toXML(a));
        a.removeOpenedProject("ruta");
        System.out.println(x.toXML(a));
    }
}