package mo.core.plugin;

import mo.core.Utils;
import mo.example.IExtPointExample;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Celso
 */
public class PluginRegistry {

    private static PluginRegistry pg;

    private List<Plugin> plugins;

    private final static String PLUGINS_FOLDER = Utils.getBaseFolder() + "/plugins";
    
    private List<String> pluginFolders;
    
    private PluginRegistry() {
        plugins = new ArrayList<>();
        pluginFolders = new ArrayList<>();
        pluginFolders.add(PLUGINS_FOLDER);
        
        //look for plugins in app jar
        
        File jarFile = new File(PluginRegistry.class
                .getProtectionDomain().getCodeSource().getLocation().getFile());
        
        processJarFile(jarFile);
        
        String[] extensions = {"class", "jar"};
        //File path

//        String[] exts = {"class"};
//        File path = new File(Utils.getBaseFolder());
//        Collection<File> files = FileUtils.listFiles(path, exts, true);
//        for (File file : files) {
//            try {
//                System.out.println(file.getName());
//                FileInputStream in = new FileInputStream(file);
//                ExtensionScanner exScanner = new ExtensionScanner(Opcodes.ASM5);
//                ClassReader cr = new ClassReader(in);
//                cr.accept(exScanner, 0);
//
//                if (exScanner.getPlugin() != null) {
//                    plugins.add(exScanner.getPlugin());
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

    private void processClassAsInputStream(InputStream classIS){
        try {
            ExtensionScanner exScanner = new ExtensionScanner(Opcodes.ASM5);
            ClassReader cr = new ClassReader(classIS);
            cr.accept(exScanner, 0);
            
            if (exScanner.getPlugin() != null) {
                plugins.add(exScanner.getPlugin());
            }
        } catch (IOException ex) {
            Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void processJarFile(String jarFilePath){
        processJarFile(new File(jarFilePath));
    }
    
    public void processJarFile(File jar){
        
        try {
            
            JarFile jarFile = new JarFile(jar);
            
            Enumeration entries = jarFile.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                
                //if(jarEntry.getName().startsWith("core/"))
                System.out.println(jarEntry.getName());
//                if (jarEntry.getName().endsWith(".jar")) {
//                    JarInputStream jarIS = null;
//                    try {
//                        jarIS = new JarInputStream(jarFile
//                                .getInputStream(jarEntry));
//                        // iterate the entries, copying the contents of each nested file
//                        // to the OutputStream
//                        JarEntry innerEntry = jarIS.getNextJarEntry();
//                        OutputStream out = System.out;
//                        while (innerEntry != null) {
//                            copyStream(jarIS, out, innerEntry);
//                            innerEntry = jarIS.getNextJarEntry();
//                        }
//                    } catch (IOException ex) {
//                        Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
//                    } finally {
//                        try {
//                            jarIS.close();
//                        } catch (IOException ex) {
//                            Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static PluginRegistry getInstance() {
        if (pg == null) {
            pg = new PluginRegistry();
        }

        return pg;
    }
    
    private static void iterateJars(String path){
        String[] exts = {"class","jar"};
        File folder = new File(Utils.getBaseFolder()+"/plugins");
        Collection<File> files = FileUtils.listFiles(folder, exts, true);
        for (File file : files) {
            System.out.println(file.getName());
        }
    }
    
    private static void cl(){
        Thread.currentThread().getContextClassLoader().toString();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        System.out.println(cl.toString());
        System.out.println(cl.getParent().toString());
        cl = PluginRegistry.class.getClassLoader();
        
        System.out.println();
    }

    public List<Plugin> getPluginsFor(String extensioPointId) {
        List<Plugin> result = new ArrayList<>();
        for (Plugin plugin : plugins) {
            for (Dependency dependency : plugin.getDependencies()) {
                if (dependency.getId().compareTo(extensioPointId) == 0) {
                    result.add(plugin);
                    break;
                }
            }
        }
        return result;
    }

    public List<Plugin> getPlugins() {
        return this.plugins;
    }

    public static Class[] getClasses(String jarName)
            throws ClassNotFoundException {
        ArrayList<Class> classes = new ArrayList<>();

        //packageName = packageName.replaceAll("\\." , "/");
        File f = new File(jarName);
        if (f.exists()) {
            try {
                JarInputStream jarFile = new JarInputStream(
                        new FileInputStream(jarName));
                JarEntry jarEntry;

                while (true) {
                    jarEntry = jarFile.getNextJarEntry();
                    
                    if (jarEntry == null) {
                        break;
                    }
                    
                    

                    String name = jarEntry.getName();
                    
                    if (name.endsWith(".class")) {
                        classes.add(Class.forName(name
                                        .replaceAll("/", "\\.")
                                        .substring(0, name.length() - 6)));
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
            }
            Class[] classesA = new Class[classes.size()];
            classes.toArray(classesA);
            return classesA;
        } else {
            return null;
        }
    }
    
    private void asd(){
        try {
            JarFile jarFile = new JarFile("");
            
            Enumeration entries = jarFile.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                
                if (jarEntry.getName().endsWith(".jar")) {
                    JarInputStream jarIS = null;
                    try {
                        jarIS = new JarInputStream(jarFile
                                .getInputStream(jarEntry));
                        // iterate the entries, copying the contents of each nested file
                        // to the OutputStream
                        JarEntry innerEntry = jarIS.getNextJarEntry();
                        OutputStream out = System.out;
                        while (innerEntry != null) {
                            copyStream(jarIS, out, innerEntry);
                            innerEntry = jarIS.getNextJarEntry();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            jarIS.close();
                        } catch (IOException ex) {
                            Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            
            
            /**
             * Read all the bytes for the current entry from the input to the output.
             */
        } catch (IOException ex) {
            Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


/**
 * Read all the bytes for the current entry from the input to the output.
 */
private void copyStream(InputStream in, OutputStream out, JarEntry entry)
        throws IOException {
    byte[] buffer = new byte[1024 * 4];
    long count = 0;
    int n = 0;
    long size = entry.getSize();
    while (-1 != (n = in.read(buffer)) && count < size) {
        out.write(buffer, 0, n);
        count += n;
    }
}

public static void testSourceCodeLocation(){
    System.out.println(PluginRegistry.class.getProtectionDomain().getCodeSource().getLocation());
}

    public static void test(){
        
        List<Plugin> myPlugins
                = PluginRegistry.getInstance().getPlugins();

        System.out.println(myPlugins.size());

        myPlugins.stream().forEach((myPlugin) -> {
            try {
                IExtPointExample o = (IExtPointExample) myPlugin.getClazz().newInstance();
                if (o instanceof IExtPointExample) {
                    o.SayHi();
                }
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(PluginRegistry.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    public static void main(String[] args) {
        //iterateJars("");
        cl();
        //test();
    }
}
