package mo.core.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Plugin {
    
    public final static Logger LOGGER = Logger.getLogger(Plugin.class.getName());

    private String id;//classname
    private String version;
    
    private String name;
    private String description;
    
    private Object instance;
    
    //private List<Plugin> dependencies;
    //private List<String> dependenciesStrs;
    private List<Dependency> dependencies;
    //private HashMap<String,String> dep;
    private String path;
    
    private Class<?> clazz;
    
    public Plugin(){
        
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Dependency> getDependencies() {
        if (dependencies == null)
            dependencies = new ArrayList<>();
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
    
    public void addDependency(Dependency dependecy){
        if (dependencies == null)
            dependencies = new ArrayList<>();
        this.dependencies.add(dependecy);
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
    
    public Object getInstance() {
        if (instance == null) {
            try {
                instance = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return instance;
    }
    
    public Object getNewInstance() {
        Object newInstance = null;
        
        try {
            newInstance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        return newInstance;
    }
    
    @Override
    public String toString(){
        String result = "";
        result += "id: " + id + ", ";
        result += "version: " + version + ", ";
        result += "name: " + name + ", ";
        result += "description: " + description + ", ";
        result += "path: " + path + ", ";
        result += "clazz: " + clazz + ", ";
        result += "dependencies: [";
        for (Dependency dep : getDependencies()) {
            result += dep.getId() + " " + dep.getVersion() + 
                    " " + dep.isPresent() + ", ";
        }
        result = result.substring(0, result.length()-2);
        result += "]";
        return result;
    }

}
