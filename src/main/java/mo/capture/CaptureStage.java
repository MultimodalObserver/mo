package mo.capture;

import bibliothek.util.xml.XAttribute;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.organization.ProjectOrganization;
import mo.organization.Stage;
import mo.organization.StageAction;
import mo.organization.StagePlugin;

@Extension(
        xtends = {
            @Extends(
                    extensionPointId = "mo.organization.Stage"
            )
        }
)
public class CaptureStage implements Stage {

    private final static Logger logger = Logger.getLogger(CaptureStage.class.getName());

    List<StagePlugin> plugins;
    ProjectOrganization organization;
    List<StageAction> actions;

    public CaptureStage() {

        plugins = new ArrayList<>();
        for (Plugin plugin : PluginRegistry.getInstance().getPluginsFor("mo.capture.CaptureProvider")) {
            CaptureProvider c = (CaptureProvider) plugin.getNewInstance();
            plugins.add(c);
        }
        
        StageAction record = new RecordAction();
        actions = new ArrayList<>();
        actions.add(record);
                
    }

    @Override
    public void setOrganization(ProjectOrganization org) {
        this.organization = org;
    }

    @Override
    public String getName() {
        return "Capture";
    }

    public void setPlugins(List<StagePlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public List<StagePlugin> getPlugins() {
        return plugins;
    }

    @Override
    public Stage fromFile(File file) {
        if (file.exists()) {
            try {
                XElement root = XIO.readUTF(new FileInputStream(file));
                XElement[] pluginsX = root.getElements("plugin");
                CaptureStage cs = new CaptureStage();
                for (XElement pluginX : pluginsX) {
                    String clazzStr = pluginX.getAttribute("class").getString();
                    String path = pluginX.getElement("path").getString();
                    File ff = new File(file.getParentFile(), path);
                    Class<?> clazz = Class.forName(clazzStr);
                    Object o = clazz.newInstance();

                    Method method = clazz.getDeclaredMethod("fromFile", File.class);

                    CaptureProvider p = (CaptureProvider) method.invoke(o, ff);
                    if (p != null) {
                        System.out.println(p.getName());
                        cs.addOrReplaceStagePlugin(p);
                    }
                    System.out.println(ff);
                }

                return cs;
            } catch (IOException | ClassNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (InstantiationException | IllegalAccessException 
                    | NoSuchMethodException | SecurityException 
                    | IllegalArgumentException | InvocationTargetException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    private void addOrReplaceStagePlugin(StagePlugin p) {
        ArrayList<StagePlugin> pluginsToReplace = new ArrayList<>();
        for (StagePlugin plugin : plugins) {
            if (plugin.getName().equals(p.getName())) {
                pluginsToReplace.add(plugin);
            }
        }
        plugins.removeAll(pluginsToReplace);
        plugins.add(p);
    }

    @Override
    public File toFile(File parent) {
        try {
            File captureFile = new File(parent, "capture.xml");
            captureFile.createNewFile();

            System.out.println("saving capture");

            XElement root = new XElement("capture");

            XElement name = new XElement("name");
            name.setString(getName());

            root.addElement(name);

            for (StagePlugin plugin : plugins) {
                if (!plugin.getConfigurations().isEmpty()) {
                    File p = new File(parent, "capture");
                    if (!p.isDirectory()) {
                        p.mkdirs();
                    }
                    File f = plugin.toFile(p);
                    if (f != null) {
                        XElement pluginX = new XElement("plugin");
                        XAttribute clazz = new XAttribute("class");
                        clazz.setString(plugin.getClass().getName());
                        pluginX.addAttribute(clazz);
                        Path filePath = parent.toPath();
                        Path selfPath = f.toPath();
                        Path relative = filePath.relativize(selfPath);
                        XElement path = new XElement("path");
                        path.setString(relative.toString());
                        pluginX.addElement(path);
                        root.addElement(pluginX);
                    }
                }
            }

            XIO.writeUTF(root, new FileOutputStream(captureFile));

            return captureFile;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<StageAction> getActions() {
        return actions;
    }

    @Override
    public String getCodeName() {
        return getName().toLowerCase();
    }
}
