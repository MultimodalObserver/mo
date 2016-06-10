package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;

/**
 *
 * @author Celso
 */
public class ClassesFinder {

    Collection<String> pathSources;
    Collection<String> fullNameClasses;
    Collection<String> interfacesToFilter;

    public ClassesFinder() {
        interfacesToFilter = new HashSet<>();
        fullNameClasses = new HashSet<>();
        pathSources = new HashSet<>();
    }

    public void search() {
        ClassReader cr;
        String exts[] = {"class"};
        
        //todo support on jar

        for (String loc : pathSources) {
            File path = new File(loc);
            Collection<File> files = FileUtils.listFiles(path, exts, true);
            for (File file : files) {
                try {
                    cr = new ClassReader(new FileInputStream(file));
                    
                    for (String iFounded : cr.getInterfaces()) {
                        for (String iLooked : interfacesToFilter) {
                            if (iFounded.compareTo(iLooked.replace(".", "/")) == 0) {
                                //System.out.println(cr.getClassName());
                                fullNameClasses.add(cr.getClassName().replace("/", "."));
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ModuleDiscover.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void addPathSource(String path) {
        if (pathSources == null) {
            pathSources = new HashSet<>();
        }
        pathSources.add(path);
    }
    
    public void addInterfaceToFilter(String i){
        if (interfacesToFilter == null)
            interfacesToFilter = new HashSet<>();
        
        interfacesToFilter.add(i);
    }

    public Collection<String> getFullNameClassesFounded() {
        return fullNameClasses;
    }

    public static void main(String[] args) {
        ClassesFinder cf = new ClassesFinder();
        File f = new File(".");
        System.out.println(f.getPath());
        System.out.println(f.getAbsolutePath());
        //System.out.println(f.getCanonicalPath());
        cf.addPathSource(f.getAbsolutePath());
        cf.addInterfaceToFilter("modules.organization.OrganizationPlugable");
        cf.search();
        for (String string : cf.getFullNameClassesFounded()) {
            System.out.println(string);
        }
    }
}
