package plugins.simpleOrganization;

import core.Extension;
import core.PluginLoader;
import modules.organization.OrganizationPlugable;

@Extension(
        codeName = "SimpleFileOrganization",
        packageName = "plugins.simpleFileOrganization",
        fullName = "plugins.SimpleFileOrganization.SimpleFileOrganization",
        name = "File Organization",
        description = "File Organization",
        interfacesThatImplements = {"modules.organization.OrganizationPlugable"},
        interfacesThatProvide = {""}
)
public class SimpleFileOrganization implements OrganizationPlugable {
    PluginLoader pl;

    public SimpleFileOrganization() {
        pl = new PluginLoader();
    }
    
}
