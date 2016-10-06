package mo.visualization.mouse;

import java.io.File;
import mo.organization.Configuration;
import mo.visualization.VisualizableConfiguration;

public class MouseVisConfiguration implements VisualizableConfiguration {
    
    String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public File toFile(File parent) {
        return new File("");
    }

    @Override
    public Configuration fromFile(File file) {
        return new MouseVisConfiguration();
    }
    
}
