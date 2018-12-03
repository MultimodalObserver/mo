package mo.communication;

import mo.capture.*;
import java.io.File;
import mo.organization.Configuration;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;

public interface StreamableConfiguration extends Configuration {
    void setupStreaming();
    void startStreaming();
    void pauseStreaming();
    void resumeStreaming();
    void stopStreaming();
    
    // los siguientes son para configurar la calidad de transmisión (en el caso del texto, el % de datos tranferidos)
    public void send25percent();
    public void send50percent();
    public void send75percent();
    public void send100percent();
    
}
