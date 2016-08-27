package mo.core.plugin;

import com.github.zafarkhaja.semver.Version;
import mo.core.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
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

    private final static String PLUGINS_FOLDER
            = Utils.getBaseFolder() + "/plugins";

    private final static String APP_PACKAGE = "mo/";

    private final List<String> pluginFolders;

    private ClassLoader cl;

    private final DirectoryWatcher dirWatcher;

    //private final Map<String, Listeners>;
    private final List<ExtPoint> extensionPoints;

    private final static Logger LOGGER
            = Logger.getLogger(PluginRegistry.class.getName());

    private PluginRegistry() {
        plugins = new ArrayList<>();
        pluginFolders = new ArrayList<>();
        extensionPoints = new ArrayList<>();
        File file = new File(PLUGINS_FOLDER);
        if (!file.isDirectory()) {
            if (file.mkdir()) {
                pluginFolders.add(PLUGINS_FOLDER);
            }
        }

        dirWatcher = new DirectoryWatcher();
        dirWatcher.addDirectory((new File(PLUGINS_FOLDER)).toPath(), true);
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
                                LOGGER.log(Level.SEVERE, null, ex);
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
            // working only with classes (Netbeans)
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
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void processJarFile(String jarFilePath) {
        processJarFile(new File(jarFilePath));
    }

    public void processJarFile(File jar) {
        processJarFile(jar, null);
    }

    public void processJarFile(File jar, String[] packages) {

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
            LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    public void addPlugin(Plugin plugin) {
        int toReplace = -1;
        String newId = plugin.getId();
        Version newVersion = Version.valueOf(plugin.getVersion());

        for (int i = 0; i < plugins.size(); i++) {

            Version v = Version.valueOf(plugins.get(i).getVersion());

            if (newId.compareTo(plugins.get(i).getId()) == 0
                    && newVersion.getMajorVersion() == v.getMajorVersion()
                    && newVersion.getMinorVersion() == v.getMinorVersion()) {
                toReplace = i;

            }
        }

        if (toReplace > -1) {
            plugins.set(toReplace, plugin);
        } else {
            plugins.add(plugin);
        }
    }

    public List<Plugin> getPluginsFor(String extensionPointId) {
        return getPluginsFor(extensionPointId, ">=0");
    }

    public List<Plugin> getPluginsFor(String extensionPointId, String version) {
        List<Plugin> result = new ArrayList<>();
        
        String xpId = null;
        for (ExtPoint extensionPoint : extensionPoints) {
            if (extensionPoint.getId().equals(extensionPointId))
                xpId = extensionPoint.getId();
        }
        
        if (xpId == null) {
            System.out.println("Id for extension point <"+extensionPointId+"> not found");
            return result;
        }
            
        
        for (Plugin plugin : plugins) {
            for (Dependency dependency : plugin.getDependencies()) {
                if (dependency.getId().compareTo(extensionPointId) == 0) {
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
        pluginFolders.stream().forEach((pluginFolder) -> {
            processFolder(pluginFolder);
        });
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
                LOGGER.log(Level.SEVERE, null, ex);
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
                    LOGGER.log(Level.SEVERE, null, ex);
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

    public void suscribeToExtensioPoint(String extensionPointId) {

    }

    public void suscribeToPlugin(String pluginId) {

    }

    public static void main(String[] args) {
        //iterateJars("");
        //test();
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
