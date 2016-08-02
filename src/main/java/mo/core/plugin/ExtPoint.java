package mo.core.plugin;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class ExtPoint {
    
    String id;
    String version;
    
    String name;
    String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString(){
        String result = "";
        result += "id: " + id + ", ";
        result += "version: " + version + ", ";
        result += "name: " + name + ", ";
        result += "description: " + description + ", ";
        return result;
    }
}
