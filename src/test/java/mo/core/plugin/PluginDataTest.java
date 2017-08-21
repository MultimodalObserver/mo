package mo.core.plugin;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author felo
 */
public class PluginDataTest {
    
    PluginData pluginData;
    static Plugin p1, p2, p3, p4, p5, p6, p12, p22, p32;
    
    @Before
    public void setUp() {
        pluginData = new PluginData();
    }
    
    @BeforeClass
    public static void setUpPlugins(){        
        p1 = new Plugin();
        p2 = new Plugin();        
        p3 = new Plugin();
        p4 = new Plugin();
        p5 = new Plugin();
        
        p12 = new Plugin();
        p22 = new Plugin();
        p32 = new Plugin();
        
        p1.setId("plugin-1");
        p1.setVersion("0.0.1");   
        
        p2.setId("plugin-2");
        p2.setVersion("2.0.1");    
        
        p3.setId("plugin-3");
        p3.setVersion("0.2.1"); 
        
        p4.setId("plugin-4");
        p4.setVersion("2.0.1");
        
        p5.setId("plugin-5");
        p5.setVersion("3.2.1");
        
        p12.setId("plugin-1");
        p12.setVersion("0.0.2");
        
        p22.setId("plugin-2");
        p22.setVersion("3.0.1");   
        
        p32.setId("plugin-3");
        p32.setVersion("0.3.1");   
        
    }
    
    
    @Test
    public void testPluginIsRegistered_NoVersion(){
        
        pluginData.addPlugin(p1);
        assertEquals(pluginData.pluginIsRegistered(p1), true);
        assertEquals(pluginData.pluginIsRegistered(p2), false);
        assertEquals(pluginData.pluginIsRegistered(p3), false);
        assertEquals(pluginData.pluginIsRegistered(p4), false);
        assertEquals(pluginData.pluginIsRegistered(p5), false);
        
        pluginData.addPlugin(p2);
        assertEquals(pluginData.pluginIsRegistered(p1), true);
        assertEquals(pluginData.pluginIsRegistered(p2), true);
        assertEquals(pluginData.pluginIsRegistered(p3), false);
        assertEquals(pluginData.pluginIsRegistered(p4), false);
        assertEquals(pluginData.pluginIsRegistered(p5), false);
        
        pluginData.addPlugin(p4);
        assertEquals(pluginData.pluginIsRegistered(p1), true);
        assertEquals(pluginData.pluginIsRegistered(p2), true);
        assertEquals(pluginData.pluginIsRegistered(p3), false);
        assertEquals(pluginData.pluginIsRegistered(p4), true);
        assertEquals(pluginData.pluginIsRegistered(p5), false);
        
        pluginData.addPlugin(p5);
        assertEquals(pluginData.pluginIsRegistered(p1), true);
        assertEquals(pluginData.pluginIsRegistered(p2), true);
        assertEquals(pluginData.pluginIsRegistered(p3), false);
        assertEquals(pluginData.pluginIsRegistered(p4), true);
        assertEquals(pluginData.pluginIsRegistered(p5), true);
        
        pluginData.addPlugin(p3);
        assertEquals(pluginData.pluginIsRegistered(p1), true);
        assertEquals(pluginData.pluginIsRegistered(p2), true);
        assertEquals(pluginData.pluginIsRegistered(p3), true);
        assertEquals(pluginData.pluginIsRegistered(p4), true);
        assertEquals(pluginData.pluginIsRegistered(p5), true);      

    }
    
    
    @Test
    public void testPluginIsRegistered_YesVersion(){
        
        pluginData.addPlugin(p1);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.2"), false);
        
        pluginData.addPlugin(p2);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.2"), false);
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.2"), false);

    }
    
    @Test
    public void testPluginIsRegistered_VersionUpdate_Minor(){
        
        pluginData.addPlugin(p1);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.2"), false);
        
        // Version 0.0.1 gets removed after adding 0.0.2
        pluginData.addPlugin(p12);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p1, "0.0.1"), false);
        assertEquals(pluginData.pluginIsRegistered(p12, "0.0.2"), true);
        assertEquals(pluginData.pluginIsRegistered(p1), false);
        assertEquals(pluginData.pluginIsRegistered(p12), true);   


    }
    
    @Test
    public void testPluginIsRegistered_VersionUpdate_Middle(){
        
        pluginData.addPlugin(p3);
        assertEquals(pluginData.pluginIsRegistered(p3, "0.2.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p3, "0.2.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p3, "0.2.2"), false);
        
        pluginData.addPlugin(p32);
        assertEquals(pluginData.pluginIsRegistered(p3, "0.2.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p3, "0.2.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p32, "0.3.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p3), true);
        assertEquals(pluginData.pluginIsRegistered(p32), true);
        assertEquals(pluginData.pluginIsRegistered(p32, "0.2.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p3, "0.3.1"), true);
    }
    
    @Test
    public void testPluginIsRegistered_VersionUpdate_Major(){
        
        pluginData.addPlugin(p2);
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.2"), false);
        
        // On major version updates, 2.0.1 won't get deleted after adding 3.0.1
        pluginData.addPlugin(p22);
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.0"), false);
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p22, "3.0.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p2), true);
        assertEquals(pluginData.pluginIsRegistered(p22), true);
        
        assertEquals(pluginData.pluginIsRegistered(p2, "2.0.1"), true);
        assertEquals(pluginData.pluginIsRegistered(p22, "3.0.1"), true);
    }
    
}
