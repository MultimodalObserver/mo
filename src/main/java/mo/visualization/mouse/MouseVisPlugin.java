package mo.visualization.mouse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.organization.Configuration;
import mo.organization.ProjectOrganization;
import mo.organization.StagePlugin;
import mo.visualization.VisualizationProvider;

@Extension(
        xtends = {
            @Extends(extensionPointId = "mo.visualization.VisualizationProvider")
        }
)
public class MouseVisPlugin implements VisualizationProvider {
    
    private final static String PLUGIN_NAME = "Mouse Visualization";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public Configuration initNewConfiguration(ProjectOrganization organization) {
        System.out.println("holi");
        return new MouseVisConfiguration();
    }

    @Override
    public List<Configuration> getConfigurations() {
        return new ArrayList<>();
    }

    @Override
    public StagePlugin fromFile(File file) {
        return new MouseVisPlugin();
    }

    @Override
    public File toFile(File parent) {
        return new File("");
    }
    
}
