package mo.core.plugin;

import mo.core.Utils;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.core.DirectoryWatcher;
import mo.core.WatchHandler;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public class PluginRegistry {

    private static PluginRegistry pg;    

    private final static String pluginsFolder
            = Utils.getBaseFolder() + "/plugins";

    private final static String APP_PACKAGE = "mo/";

    private final List<String> pluginFolders;

    private ClassLoader cl;

    private final DirectoryWatcher dirWatcher;   
    
    private final PluginData pluginData;
    
    private boolean testingJar = false;
    

    private final static Logger logger
            = Logger.getLogger(PluginRegistry.class.getName());
    
    private ArrayList<ClassLoader> classLoaders;

    private PluginRegistry() {
        
        pluginFolders = new ArrayList<>();
        pluginData = new PluginData();        
        dirWatcher = new DirectoryWatcher();
        
        File folder = new File(pluginsFolder);
        if (!folder.isDirectory()) {
            if (!folder.mkdir()) {
                logger.log(
                        Level.WARNING, 
                        "Can not create plugins folder \"{0}\"", pluginsFolder);
            }
        }
        
        pluginFolders.add(pluginsFolder);
        dirWatcher.addDirectory(folder.toPath(), true);
        
    }
    
    public PluginData getPluginData(){
        return pluginData;
    }
       
  
    

    public synchronized static PluginRegistry getInstance() {
        if (pg == null) {
            pg = new PluginRegistry();            
            
            try{
                //look for plugins in app jar
                pg.processAppJar();
                
                //look for plugins in folders
                pg.processPluginFolders();
                
            } catch(Exception e){
                e.printStackTrace();
            }
            

            pg.dirWatcher.addWatchHandler(new WatchHandler() {
                @Override
                public void onCreate(File file) {
                    try {
                        processFile(file);
                    } catch(Exception e){
                        e.printStackTrace();
                    }                    
                }

                @Override
                public void onDelete(File file) {
                    // nothing
                }

                @Override
                public void onModify(File file) {
                    try {
                        processFile(file);
                    } catch(Exception e){
                        e.printStackTrace();
                    }   
                }
            });            

            pg.dirWatcher.start();

            pg.pluginData.checkDependencies();
        }

        return pg;
    }
    
    
    
    private static void processFile(File file) throws IOException {
        
        if (file.isFile()) {
            if (file.getName().endsWith(".class")) {
                FileInputStream in = new FileInputStream(file);
                
                pg.processClassAsInputStream(in, true);

            } else if (file.getName().endsWith(".jar")) {
                pg.processJarFile(file);
            }
        } else {
            pg.processFolder(file.getAbsolutePath());
        }
        pg.pluginData.checkDependencies();
    }
    

    private void processAppJar() throws IOException {
        File jarFile = new File(PluginRegistry.class.getProtectionDomain()
                .getCodeSource().getLocation().getFile());

        String[] packages = {APP_PACKAGE};

        if (jarFile.getName().endsWith(".jar")) {
            processJarFile(jarFile, packages);
        } else {
            // working only with classes (development)(Netbeans)
            pluginFolders.add(0, jarFile.getAbsolutePath());
        }
    }

    private void processClassAsInputStream(InputStream classIS, boolean external) throws IOException {

        ExtensionScanner exScanner = new ExtensionScanner(Opcodes.ASM5);
        exScanner.setClassLoader(cl);
        ClassReader cr = new ClassReader(classIS);
        cr.accept(exScanner, 0);

        if(testingJar){
            return;
        }

        if (exScanner.getPlugin() != null) {
            Plugin newPlugin = exScanner.getPlugin();
            newPlugin.setExternal(external);
            pg.pluginData.addPlugin(newPlugin);

        } else if (exScanner.getExtPoint() != null) {
            pg.pluginData.addExtensionPoint(exScanner.getExtPoint());

        }

    }


    private void processJarFile(File jar) throws IOException {
        processJarFile(jar, null);
    }

    private void processJarFile(File jar, String[] packages) throws IOException {

        JarFile jarFile = new JarFile(jar);

        cl = URLClassLoader.newInstance(new URL[] {jar.toURI().toURL()}, PluginRegistry.class.getClassLoader());

        Enumeration entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) entries.nextElement();
            String entryName = jarEntry.getName();

            if (entryName.endsWith(".class")) {
                if (packages != null) {
                    for (String p : packages) {
                        if (entryName.startsWith(p)) {
                            processClassAsInputStream(jarFile.getInputStream(jarEntry), true);
                        }
                    }
                } else {
                    processClassAsInputStream(jarFile.getInputStream(jarEntry), true);
                }
            }
        }
    }

    
    
    public Exception checkPlugin(File file){
        
        testingJar = true;        
        
        try{            
            processFile(file);
            return null;            
        } catch(Exception e){            
            return e;            
        } finally {
            testingJar = false;
        }        
    }

  
    public void copyPluginToFolder(File file) throws IOException{
        Path src = Paths.get(file.getAbsolutePath());
        Path dest = Paths.get(pluginsFolder+"/");        
        Files.copy(src, dest.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
    }
    

    private void processPluginFolders() throws IOException {
        for (String pluginFolder : pluginFolders) {
            processFolder(pluginFolder);
        }
    }
    

    private void processFolder(String pluginFolder) throws IOException, FileNotFoundException, MalformedURLException {
        String[] extensions = {"class", "jar"};
        ClassLoader classLoader = PluginRegistry.class.getClassLoader();
        URLClassLoader urlCL;
        List<URL> urls = new ArrayList<>();
        Collection<File> files = FileUtils
                .listFiles(new File(pluginFolder), extensions, true);
        for (File f : files) {
            if (f.getName().endsWith(".class")) {
                urls.add(f.getParentFile().toURI().toURL());
            } else if (f.getName().endsWith(".jar")) {
                urls.add(f.toURI().toURL());
            }
        }

        urlCL = new URLClassLoader(
                (URL[]) urls.toArray(new URL[urls.size()]), classLoader);
        
        if (classLoaders == null) {
            classLoaders = new ArrayList<>();
        }
        classLoaders.add(urlCL);
        
        cl = urlCL;

        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                processClassAsInputStream(
                            new FileInputStream(file), false);
            } else if (file.getName().endsWith(".jar")) {
                processJarFile(file);
            }
        }
    }


    
    
    public List<ClassLoader> getClassLoaders() {
        return classLoaders;
    }

    public Class<?> getClassForName(String clazzStr) throws ClassNotFoundException {
        
        ClassLoader cd;
        
        cd = PluginRegistry.class.getClassLoader();
        
        Class<?> c = null;
        
        try {
            c = cd.loadClass(clazzStr);
        } catch (ClassNotFoundException ex) {
            for (ClassLoader classLoader : classLoaders) {
                try {
                    c = classLoader.loadClass(clazzStr);
                } catch (ClassNotFoundException ex1) {
                    // blank
                }
                if (c != null) {
                    break;
                }
            }
        }
        
        if (c == null) {
            throw new ClassNotFoundException(clazzStr);
        }
        
        return c;
    }
}
