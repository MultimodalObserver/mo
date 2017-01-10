package mo.capture.theeyetribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mo.capture.CaptureProvider;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.organization.Configuration;
import mo.organization.ProjectOrganization;
import mo.organization.StagePlugin;

@Extension(
    xtends = {
        @Extends(extensionPointId = "mo.capture.CaptureProvider")
    }
)
public class TheEyetribeCapturePlugin implements CaptureProvider {

    @Override
    public String getName() {
        return "The Eye Tribe";
    }

    @Override
    public Configuration initNewConfiguration(ProjectOrganization organization) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Configuration> getConfigurations() {
        return new ArrayList<>();
    }

    @Override
    public StagePlugin fromFile(File file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File toFile(File parent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
