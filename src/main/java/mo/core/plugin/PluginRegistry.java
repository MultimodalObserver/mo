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
            
            //look for plugins in app jar
            pg.processAppJar();
            
            //look for plugins in folders
            pg.processPluginFolders();

            pg.dirWatcher.addWatchHandler(new WatchHandler() {
                @Override
                public void onCreate(File file) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".class")) {
                            try (FileInputStream in = new FileInputStream(file)) {
                                pg.processClassAsInputStream(in, true);
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        } else if (file.getName().endsWith(".jar")) {
                            pg.processJarFile(file);
                        }
                    } else {
                        pg.processFolder(file.getAbsolutePath());
                    }
                    pg.pluginData.checkDependencies();
                }

                @Override
                public void onDelete(File file) {
                    // nothing
                }

                @Override
                public void onModify(File file) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".class")) {
                            try (FileInputStream in = new FileInputStream(file)) {
                                pg.processClassAsInputStream(in, true);
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        } else if (file.getName().endsWith(".jar")) {
                            pg.processJarFile(file);
                        }
                    } else {
                        pg.processFolder(file.getAbsolutePath());
                    }
                    pg.pluginData.checkDependencies();
                }
            });

            pg.dirWatcher.start();

            pg.pluginData.checkDependencies();
        }

        return pg;
    }

    private void processAppJar() {
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

    private void processClassAsInputStream(InputStream classIS, boolean external) {
        try {
            ExtensionScanner exScanner = new ExtensionScanner(Opcodes.ASM5);
            exScanner.setClassLoader(cl);
            ClassReader cr = new ClassReader(classIS);
            cr.accept(exScanner, 0);

            if (exScanner.getPlugin() != null) {
                Plugin newPlugin = exScanner.getPlugin();
                newPlugin.setExternal(external);
                pg.pluginData.addPlugin(newPlugin);
                //logger.info(exScanner.getPlugin()+ " added.");
            } else if (exScanner.getExtPoint() != null) {
                pg.pluginData.addExtensionPoint(exScanner.getExtPoint());
                //logger.info(exScanner.getExtPoint()+ " added.");
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }


    private void processJarFile(File jar) {
        processJarFile(jar, null);
    }

    private void processJarFile(File jar, String[] packages) {

        try (JarFile jarFile = new JarFile(jar)) {

            cl = URLClassLoader.newInstance(new URL[] {jar.toURI().toURL()}, PluginRegistry.class.getClassLoader());
            
            Enumeration entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                String entryName = jarEntry.getName();

                if (entryName.endsWith(".class")) {
                    if (packages != null) {
                        for (String p : packages) {
                            if (entryName.startsWith(p)) {
                                processClassAsInputStream(jarFile
                                        .getInputStream(jarEntry), true);
                            }
                        }
                    } else {
                        processClassAsInputStream(
                                jarFile.getInputStream(jarEntry), true);
                    }
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

  
    public void copyPluginToFolder(File file) throws IOException{
        Path src = Paths.get(file.getAbsolutePath());
        Path dest = Paths.get(pluginsFolder+"/");        
        Files.copy(src, dest.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
    }
    

    private void processPluginFolders() {
        for (String pluginFolder : pluginFolders) {
            processFolder(pluginFolder);
        }
    }
    

    private void processFolder(String pluginFolder) {
        String[] extensions = {"class", "jar"};
        ClassLoader classLoader = PluginRegistry.class.getClassLoader();
        URLClassLoader urlCL;
        List<URL> urls = new ArrayList<>();
        Collection<File> files = FileUtils
                .listFiles(new File(pluginFolder), extensions, true);
        for (File f : files) {
            try {
                if (f.getName().endsWith(".class")) {
                    urls.add(f.getParentFile().toURI().toURL());
                } else if (f.getName().endsWith(".jar")) {
                    urls.add(f.toURI().toURL());
                }
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, null, ex);
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
                try {
                    processClassAsInputStream(
                            new FileInputStream(file), false);
                } catch (FileNotFoundException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
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
