package mo.core.plugin;

import com.github.zafarkhaja.semver.Version;
import mo.core.Utils;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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

    private final List<Plugin> plugins;

    private final static String pluginsFolder
            = Utils.getBaseFolder() + "/plugins";

    private final static String APP_PACKAGE = "mo/";

    private final List<String> pluginFolders;

    private ClassLoader cl;

    private final DirectoryWatcher dirWatcher;

    //private final Map<String, Listeners>;
    private final List<ExtPoint> extensionPoints;
    
    private final HashMap<String, List<PluginListener>> pluginListeners;

    private final static Logger logger
            = Logger.getLogger(PluginRegistry.class.getName());

    private PluginRegistry() {
        plugins = new ArrayList<>();
        pluginFolders = new ArrayList<>();
        extensionPoints = new ArrayList<>();
        pluginListeners = new HashMap<>();
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
                                pg.processClassAsInputStream(in);
                            } catch (IOException ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        } else if (file.getName().endsWith(".jar")) {
                            pg.processJarFile(file);
                        }
                    } else {
                        pg.processFolder(file.getAbsolutePath());
                    }
                }

                @Override
                public void onDelete(File file) {
                    // nothing
                }

                @Override
                public void onModify(File file) {
                    // nothing
                }
            });

            pg.dirWatcher.start();

            pg.checkDependencies();
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

    private void processClassAsInputStream(InputStream classIS) {
        try {
            ExtensionScanner exScanner = new ExtensionScanner(Opcodes.ASM5);
            exScanner.setClassLoader(cl);
            ClassReader cr = new ClassReader(classIS);
            cr.accept(exScanner, 0);

            if (exScanner.getPlugin() != null) {
                addPlugin(exScanner.getPlugin());
            } else if (exScanner.getExtPoint() != null) {
                addExtensionPoint(exScanner.getExtPoint());
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void processJarFile(String jarFilePath) {
        processJarFile(new File(jarFilePath));
    }

    private void processJarFile(File jar) {
        processJarFile(jar, null);
    }

    private void processJarFile(File jar, String[] packages) {

        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) entries.nextElement();
                String entryName = jarEntry.getName();

                if (entryName.endsWith(".class")) {
                    if (packages != null) {
                        for (String p : packages) {
                            if (entryName.startsWith(p)) {
                                processClassAsInputStream(jarFile
                                        .getInputStream(jarEntry));
                            }
                        }
                    } else {
                        processClassAsInputStream(
                                jarFile.getInputStream(jarEntry));
                    }
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

    }

    public void addPlugin(Plugin plugin) {
        int toReplace = -1;
        String newId = plugin.getId();
        Version newVersion = Version.valueOf(plugin.getVersion());

        for (int i = 0; i < plugins.size(); i++) {

            Version v = Version.valueOf(plugins.get(i).getVersion());

            if (newId.equals(plugins.get(i).getId())
                    && newVersion.getMajorVersion() == v.getMajorVersion()
                    && newVersion.getMinorVersion() == v.getMinorVersion()) {
                toReplace = i;

            }
        }

        if (toReplace > -1) {
            plugins.set(toReplace, plugin);
            for (Dependency dependency : plugin.getDependencies()) {
                if (pluginListeners.containsKey(dependency.getId())) {
                    for (PluginListener pluginListener : pluginListeners.get(dependency.getId())) {
                        pluginListener.pluginUpdated(plugin);
                    }
                }
            }
        } else {
            plugins.add(plugin);
            for (Dependency dependency : plugin.getDependencies()) {
                if (pluginListeners.containsKey(dependency.getId())) {
                    for (PluginListener pluginListener : pluginListeners.get(dependency.getId())) {
                        pluginListener.pluginAdded(plugin);
                    }
                }
            }
        }
        
        //logger.log(Level.INFO,"Plugin added: {0}", plugin);
    }

    public List<Plugin> getPluginsFor(String extensionPointId) {
        return getPluginsFor(extensionPointId, ">=0.0.0");
    }

    public List<Plugin> getPluginsFor(String extensionPointId, String version) {
        List<Plugin> result = new ArrayList<>();
        
        String xpId = null;
        for (ExtPoint extensionPoint : extensionPoints) {
            if (extensionPoint.getId().equals(extensionPointId)) {
                Version v = Version.valueOf(extensionPoint.getVersion());
                if (v.satisfies(version)) {
                    xpId = extensionPoint.getId();
                }
            }
        }
        
        if (xpId == null) {
            logger.log(Level.INFO, 
                    "Id for extension point <{0}> not found", 
                    extensionPointId );
            return result;
        }
            
        
        for (Plugin plugin : plugins) {
            for (Dependency dependency : plugin.getDependencies()) {
                if (dependency.getId().equals(extensionPointId)) {
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

        cl = urlCL;

        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                try {
                    processClassAsInputStream(
                            new FileInputStream(file));
                } catch (FileNotFoundException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } else if (file.getName().endsWith(".jar")) {
                processJarFile(file);
            }
        }
    }

    public Object getPluginInstance(String pluginId) {
        for (Plugin plugin : plugins) {
            if (pluginId.equals(plugin.getId())) {
                return plugin.getInstance();
            }
        }
        return null;
    }

    public Plugin getPlugin(String pluginId) {
        for (Plugin plugin : plugins) {
            if (pluginId.equals(plugin.getId())) {
                return plugin;
            }
        }
        return null;
    }

    public List<ExtPoint> getExtPoints() {
        return extensionPoints;
    }

    public void addPluginListener(String extensionPointId, PluginListener listener) {
        if (!pluginListeners.containsKey(extensionPointId)) {
            ArrayList<PluginListener> xpListeners = new ArrayList<>();
            xpListeners.add(listener);
            pluginListeners.put(extensionPointId, xpListeners);
        } else {
            pluginListeners.get(extensionPointId).add(listener);
        }       
    }

    public void removePluginListener(String extensionPointId, PluginListener listener) {
        if (pluginListeners.containsKey(extensionPointId)) {
            List l = pluginListeners.get(extensionPointId);
            if (l.contains(listener)) {
                l.remove(l);
            }
        }
    }
    
    private void addExtensionPoint(ExtPoint extPoint) {
        extensionPoints.add(extPoint);
    }

    private void checkDependencies() {
        for (Plugin plugin : plugins) {
            for (Dependency dependency : plugin.getDependencies()) {
                for (ExtPoint extensioPoint : extensionPoints) {
                    if (dependency.getId().equals(extensioPoint.getId())) {
                        dependency.setExtensionPoint(extensioPoint);
                        dependency.setIsPresent(true);
                        extensioPoint.addPlugin(plugin);
                        break;
                    }
                }
            }
        }
    }
}
