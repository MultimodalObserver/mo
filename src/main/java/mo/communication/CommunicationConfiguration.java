package mo.communication;

import java.io.File;

public interface CommunicationConfiguration {
    void showPlayer();
    void closePlayer();
    void setInfo(File parentDir, String name);
}
