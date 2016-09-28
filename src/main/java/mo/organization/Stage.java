package mo.organization;

import java.io.File;
import java.util.List;
import mo.core.plugin.ExtensionPoint;

@ExtensionPoint
public interface Stage {
    
    String getCodeName();
    String getName();
    List<StagePlugin> getPlugins();
    Stage fromFile(File file);
    File toFile(File parent);
    void setOrganization(ProjectOrganization org);
    List<StageAction> getActions();
}
