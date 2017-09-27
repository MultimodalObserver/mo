package mo.analysis;

import java.util.ArrayList;
import mo.core.ui.dockables.DockablesRegistry;
import mo.organization.Configuration;
import mo.organization.StagePlugin;

import mo.organization.StageAction;
import mo.organization.ProjectOrganization;
import mo.organization.Participant;
import mo.organization.StageModule;
import mo.visualization.VisualizationDialog2;
import mo.visualization.VisualizationPlayer;
import mo.visualization.VisualizableConfiguration;


public class AnalyzeAction implements StageAction {

    public static void main(String[] args) {
        
    }
    
    @Override
    public String getName() {
        return "Analyze";
    }

    @Override
    public void init(ProjectOrganization organization, Participant participant, StageModule stage) {
        ArrayList<Configuration> configs = new ArrayList<>();
        for (StageModule astage : organization.getStages()) {
            if(astage.getCodeName().equals("visualization")) {
                for(StagePlugin aplugin : astage.getPlugins()) {
                    for(Configuration aconfiguration : aplugin.getConfigurations()) {
                        configs.add(aconfiguration);
                    }
                }
            }
        }
        for (StagePlugin plugin : stage.getPlugins()) {
            for (Configuration configuration : plugin.getConfigurations()) {
                configs.add(configuration);
            }
        }
        
        AnalysisDialog d = new AnalysisDialog(configs, organization.getLocation());
        boolean accept = d.show();
        
        if (accept) {
            
            VisualizationPlayer player = new VisualizationPlayer(d.getConfigurations());
            DockablesRegistry.getInstance().addDockableInProjectGroup(organization.getLocation().getAbsolutePath(),player.getDockable());
            DockablesRegistry.getInstance().addDockableInProjectGroup(organization.getLocation().getAbsolutePath(),player.getTimePanelDockable());
        }
    }
}