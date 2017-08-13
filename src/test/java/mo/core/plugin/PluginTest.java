/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mo.core.plugin;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author felo
 */
public class PluginTest {
    
    @Test
    public void testEquality() {
        
        Plugin A = new Plugin();
        A.setId("A");
        
        Plugin B = new Plugin();
        B.setId("B");
        
        Plugin C = new Plugin();
        C.setId("A");
        
        assertEquals(A, C);       
        assertEquals(A.equals(C), true);
        
        assertEquals(C, A);       
        assertEquals(C.equals(A), true);        
  
        assertEquals(A.equals(B), false);
        assertEquals(B.equals(C), false);
        assertEquals(B.equals(A), false);
        assertEquals(A.equals(B), false);
        
    }
    
}
