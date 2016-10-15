package mo.visualization.eyetracker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.organization.Configuration;
import mo.visualization.VisualizableConfiguration;
import mo.visualization.keyboard.KBVisConfig;

public class FixationConfig implements VisualizableConfiguration {
    private final String[] creators = {"mo.capture.eyetribe.EyeTribeRecorder"};
    private List<File> files;
    private String id;
    private EyeTribeFixPlayer player;
    
    private static final Logger logger = Logger.getLogger(KBVisConfig.class.getName());

    public FixationConfig() {
        files = new ArrayList<>();
    }

    @Override
    public List<String> getCompatibleCreators() {
        return Arrays.asList(creators);
    }

    @Override
    public void addFile(File file) {
        if (!files.contains(file)) {
            files.add(file);
        }
    }

    @Override
    public void removeFile(File file) {
        if (files.contains(file)) {
            files.remove(file);
        }
    }
    
    @Override
    public File toFile(File parent) {
        File f = new File(parent, "eyeFixation-visualization_"+id+".xml");
        try {
            f.createNewFile();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return f;
    }

    @Override
    public Configuration fromFile(File file) {
        String fileName = file.getName();

        if (fileName.contains("_") && fileName.contains(".")) {
            String name = fileName.substring(fileName.indexOf("_")+1, fileName.lastIndexOf("."));
            FixationConfig config = new FixationConfig();
            config.id = name;
            return config;
        }
        return null;
    }

    private void ensurePlayerExistence() {
        if (player == null) {
            player = new EyeTribeFixPlayer(files.get(0));
        }
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setSpeed(double factor) {
        ensurePlayerExistence();
        player.setSpeed(factor);
    }

    @Override
    public void pause() {
        ensurePlayerExistence();
        player.pause();
    }

    @Override
    public void seek(long millis) {
        ensurePlayerExistence();
        player.seek(millis);
    }

    @Override
    public long getStart() {
        ensurePlayerExistence();
        return player.getStart();
    }

    @Override
    public long getEnd() {
        ensurePlayerExistence();
        return player.getEnd();
    }

    @Override
    public void play() {
        ensurePlayerExistence();
        player.play();
    }
}