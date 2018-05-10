package mo.visualization;

import java.util.ArrayList;
import mo.core.ui.dockables.DockablesRegistry;
import mo.organization.Configuration;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;
import mo.organization.StageAction;
import mo.organization.StagePlugin;
import mo.organization.StageModule;
import mo.core.I18n;

public class VisualizeAction implements StageAction {
    private I18n i18n;

    public VisualizeAction() {
        i18n = new I18n(VisualizeAction.class);
    }

    public static void main(String[] args) {
        
    }
    
    @Override
    public String getName() {
        return i18n.s("VisualizationStage.visualize");
    }

    @Override
    public void init(ProjectOrganization organization, Participant participant, StageModule stage) {
        
        ArrayList<Configuration> configs = new ArrayList<>();
        for (StagePlugin plugin : stage.getPlugins()) {
            for (Configuration configuration : plugin.getConfigurations()) {
                configs.add(configuration);
            }
        }
        
        VisualizationDialog2 d = new VisualizationDialog2(configs, organization.getLocation());
        boolean accept = d.show();
        
        if (accept) {
            VisualizationPlayer p = new VisualizationPlayer(d.getConfigurations());
            DockablesRegistry.getInstance()
                    .addDockableInProjectGroup(
                            organization.getLocation().getAbsolutePath(),
                            p.getDockable());
        }
    }
    
}
