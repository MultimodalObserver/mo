package mo.visualization;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
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
public class VisualizationStage implements Stage {
    
    List<StagePlugin> plugins;
    List<StageAction> actions;
    ProjectOrganization organization;
    
    private static final Logger logger 
            = Logger.getLogger(VisualizationStage.class.getName());

    public VisualizationStage() {
        plugins = new ArrayList<>();
        actions = new ArrayList<>();
    }
    
    @Override
    public String getCodeName() {
        return getName().toLowerCase();
    }

    @Override
    public String getName() {
        return "Visualization";
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
                VisualizationStage visStage = new VisualizationStage();
                for (XElement pluginX : pluginsX) {
                    String clazzStr = pluginX.getAttribute("class").getString();
                    String path = pluginX.getElement("path").getString();
                    File ff = new File(file.getParentFile(), path);
                    Class<?> clazz = Class.forName(clazzStr);
                    Object o = clazz.newInstance();

                    Method method = clazz.getDeclaredMethod("fromFile", File.class);

                    VisualizationProvider p 
                            = (VisualizationProvider) method.invoke(o, ff);
                    if (p != null) {
                        //System.out.println(p.getName());
                        //cs.addOrReplaceStagePlugin(p);
                    }
                    System.out.println(ff);
                }

                return visStage;
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

    @Override
    public File toFile(File parent) {
        return new File("example");
    }

    @Override
    public void setOrganization(ProjectOrganization org) {
        this.organization = org;
    }

    @Override
    public List<StageAction> getActions() {
        return actions;
    }
    
}
