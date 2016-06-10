package core;

import java.util.HashSet;

/**
 *
 * @author Celso
 */
public class ClassContainer {
    String fullClassName;
    String name;
    String description;
    String packageName;
    HashSet<String> interfacesThatImplements;
    HashSet<String> interfacesThatProvide;
    
    public ClassContainer(String fullClassName){
        this.fullClassName = fullClassName;
        name = fullClassName.substring(fullClassName.lastIndexOf("."));
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public HashSet<String> getInterfacesThatImplements() {
        return interfacesThatImplements;
    }

    public void setInterfacesThatImplements(HashSet<String> interfacesThatImplements) {
        this.interfacesThatImplements = interfacesThatImplements;
    }

    public HashSet<String> getInterfacesThatProvide() {
        return interfacesThatProvide;
    }

    public void setInterfacesThatProvide(HashSet<String> interfacesThatProvide) {
        this.interfacesThatProvide = interfacesThatProvide;
    }
    
    
}
