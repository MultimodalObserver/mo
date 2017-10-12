package mo.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.organization.Configuration;
import mo.visualization.Playable;
import mo.visualization.VisualizableConfiguration;
import mo.analysis.AnalyzableConfiguration;
import mo.organization.ProjectOrganization;
import mo.organization.Participant;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
import javax.swing.JPanel;

public class NotesAnalysisConfig implements PlayableAnalyzableConfiguration {

    private final String[] creators = {"alonsho"};
    
    private List<File> files;
    private String id;
    private NotesPlayer player;
    
    private static final Logger logger = Logger.getLogger(NotesAnalysisConfig.class.getName());
    private boolean stopped;

    public NotesAnalysisConfig() {
        files = new ArrayList<>();
    }
    
    public void setId(String id) {
        this.id = id;
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
        File toRemove = null;
        for (File f : files) {
            if (f.equals(file)) {
                toRemove = f;
            }
        }
        
        if (toRemove != null) {
            files.remove(toRemove);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public File toFile(File parent) {
        File f = new File(parent, "notes-analysis_"+id+".xml");
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
            NotesAnalysisConfig config = new NotesAnalysisConfig ();
            config.id = name;
            return config;
        }

        return null;
    }

    @Override 
    public void startAnalysis() {
        try {
            System.out.println("startAnalysis");
            Thread.sleep(1000);
            System.out.println("startAnalysis");
            Thread.sleep(1000);
            System.out.println("startAnalysis");
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void cancelAnalysis() {
        Thread.currentThread().interrupt();
    }

    @Override
    public void setupAnalysis(File stageFolder, ProjectOrganization org, Participant p) {
        try {
            System.out.println("setupAnalysis");
            Thread.sleep(1000);
            System.out.println("setupAnalysis");
            Thread.sleep(1000);
            System.out.println("setupAnalysis");
            Thread.sleep(1000);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Playable getPlayer() {
        if (player == null) {
            player = new NotesPlayer();
        }

        return player;
    }
}
