package mo.communication.notes;

import mo.communication.CommunicationConfiguration;
import mo.communication.CommunicationProvider1;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;

@Extension(
        xtends = {
            @Extends(extensionPointId = "mo.communication.CommunicationProvider1")
        }
)
public class RemoteNotesPlugin implements CommunicationProvider1{

    CommunicationConfiguration config;
    @Override
    public String getName() {
        return "Remote Notes Receiver";
    }

    @Override
    public CommunicationConfiguration initNewConfiguration(String id) {
        config = new RemoteNotes(id);
        return config;
    }
    
}
