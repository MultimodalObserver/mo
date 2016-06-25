package mo.pruebas;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author Celso
 */
public class Preferences2 {
    XStream s;
    public Preferences2(File f){
        s = new XStream();

    }
    
    public void save(Object o){
        System.out.println(s.toXML(o));
    }
    
    public static void main(String[] args) throws BackingStoreException{
        Preferences p = Preferences.userRoot();
        
        p.sync();
        p.putInt("asd", 123);
        p.put("asd.projects", "asd");
        p.flush();
    }
}
