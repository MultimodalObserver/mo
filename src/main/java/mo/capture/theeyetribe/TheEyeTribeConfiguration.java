package mo.capture.theeyetribe;

import java.io.File;
import mo.capture.RecordableConfiguration;
import mo.organization.Configuration;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;

public class TheEyetribeConfiguration implements RecordableConfiguration {

    @Override
    public void setupRecording(File stageFolder, ProjectOrganization org, Participant p) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startRecording() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cancelRecording() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pauseRecording() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resumeRecording() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopRecording() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File toFile(File parent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Configuration fromFile(File file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
