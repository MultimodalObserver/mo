package mo.core.plugin;

/**
 *
 * @author Celso
 */
public class Dependency {
    private String id;
    private String version;
    private boolean present;

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

    public boolean isPresent() {
        return present;
    }

    public void setIsPresent(boolean isPresent) {
        this.present = isPresent;
    }
}
