package mo.core.plugin.gui;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author felo
 */
public class RemotePluginInstallerTest {
    
    @Test
    public void cleansUrlCorrectly(){

        RemotePluginInstaller r = new RemotePluginInstaller();        
      
        assertEquals(r.cleanServerUrl("http://localhost:3000"), "http://localhost:3000");
        assertEquals(r.cleanServerUrl("http://localhost:3000/"), "http://localhost:3000");
        
        assertEquals(r.cleanServerUrl("   http://localhost:3000"), "http://localhost:3000");
        assertEquals(r.cleanServerUrl("http://localhost:3000/   "), "http://localhost:3000");
        assertEquals(r.cleanServerUrl("   http://localhost:3000   "), "http://localhost:3000");
        
        assertEquals(r.cleanServerUrl("https://localhost:3000"), "https://localhost:3000");
        assertEquals(r.cleanServerUrl("https://localhost:3000/"), "https://localhost:3000");


    }
    
}
