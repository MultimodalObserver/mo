package mo.capture.eeg;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final Logger logger = Logger.getLogger(EEGCapturePlugin.class.getName());

    public EEGCapturePlugin() {
        configurations = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "MindWave";
    }

    @Override
    public Configuration initNewConfiguration(ProjectOrganization organization) {
        EEGConfigurationDialog dialog
                = new EEGConfigurationDialog(organization);

        boolean accepted = dialog.showDialog();

        if (accepted) {
            EEGConfiguration configuration
                    = new EEGConfiguration(dialog.getConfigurationName());

            configurations.add(configuration);
            return configuration;
        }

        return null;
    }

    @Override
    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public StagePlugin fromFile(File file) {
        if (file.isFile()) {
            try {
                EEGCapturePlugin mc = new EEGCapturePlugin();
                XElement root = XIO.readUTF(new FileInputStream(file));
                XElement[] pathsX = root.getElements("path");
                for (XElement pathX : pathsX) {
                    String path = pathX.getString();
                    EEGConfiguration c = new EEGConfiguration("");
                    Configuration config = c.fromFile(new File(file.getParentFile(), path));
                    if (config != null) {
                        mc.configurations.add(config);
                    }
                }
                return mc;
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public File toFile(File parent) {
        File file = new File(parent, "eeg-capture.xml");
        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        XElement root = new XElement("capturers");
        for (Configuration config : configurations) {
            File p = new File(parent, "eeg-capture");
            p.mkdirs();
            File f = config.toFile(p);

            XElement path = new XElement("path");
            Path parentPath = parent.toPath();
            Path configPath = f.toPath();
            path.setString(parentPath.relativize(configPath).toString());
            root.addElement(path);
        }
        try {
            XIO.writeUTF(root, new FileOutputStream(file));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return file;
    }

}
