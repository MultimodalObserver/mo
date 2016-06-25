package mo.core;

import java.io.File;
import java.util.Collection;

public class PluginLoader {
    private Collection<Class> extensionClasses;
    private Collection<String> extensionClassesNames;

    public Collection<Class> getExtensionClasses() {
        return extensionClasses;
    }

    public void setExtensionClasses(Collection<Class> extensionClasses) {
        this.extensionClasses = extensionClasses;
    }
    
    public void setExtensionClassesNames(Collection<String> extensionClassesNames) {
        this.extensionClassesNames = extensionClassesNames;
    }
    
    public PluginLoader(){
    }
    
    public void addSource(File folder){
    
    }
    
    public void addSource(){
    
    }
}
