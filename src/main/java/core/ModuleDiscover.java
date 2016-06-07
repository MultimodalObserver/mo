package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;

public class ModuleDiscover {

    static final String MODULES_PACKAGE = "modules/";
    Collection<String> modulesLocations;
    Collection<String> modulesFullNameClasses;

    ModuleDiscover() {
        modulesFullNameClasses = new ArrayList<>();
        File modulesLocation = new File(ModuleDiscover.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath() + MODULES_PACKAGE);
        addModulesLocation(modulesLocation.getPath());
        findClassesThatImplements(Modulable.class, modulesLocations);
    }

    public void findClassesThatImplements(Class interfaceClass, Collection<String> paths) {
        
        ClassReader cr;
        String exts[] = {"class"};
        String interfaceClassName = 
                interfaceClass.getCanonicalName().replace(".", "/");
        
        for (String loc : paths) {
            File path = new File(loc);
            Collection<File> files = FileUtils.listFiles(path, exts, true);
            for (File file : files) {
                try {
                    cr = new ClassReader(new FileInputStream(file));
                    for (String i : cr.getInterfaces()) {
                                                
                        if (i.compareTo(interfaceClassName) == 0) {
                            //System.out.println(cr.getClassName());
                            modulesFullNameClasses.add(cr.getClassName());
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ModuleDiscover.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void addModulesLocation(String path) {
        if (modulesLocations == null) {
            modulesLocations = new ArrayList<>();
        }
        modulesLocations.add(path);
    }

    public static void main(String[] args) {
        ModuleDiscover md = new ModuleDiscover();
        md.findClassesThatImplements(Modulable.class, md.modulesLocations);
    }

    public Collection<String> getModulesFullNameClasses() {
        return modulesFullNameClasses;
    }

}
