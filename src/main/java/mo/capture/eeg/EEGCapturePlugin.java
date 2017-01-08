package mo.capture.eeg;

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
public class EEGCapturePlugin implements CaptureProvider {

    private List<Configuration> configurations;
    
    public EEGCapturePlugin() {
        configurations = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "MindWave";
    }

    @Override
    public Configuration initNewConfiguration(ProjectOrganization organization) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Configuration> getConfigurations() {
        return configurations;
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
