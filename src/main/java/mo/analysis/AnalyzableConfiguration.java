package mo.analysis;

import java.io.File;
import mo.organization.Configuration;
import mo.organization.ProjectOrganization;
import mo.organization.Participant;

public interface AnalyzableConfiguration extends Configuration {
    void setupAnalysis(File stageFolder, ProjectOrganization org, Participant p);
    void startAnalysis();
    void cancelAnalysis();
}
