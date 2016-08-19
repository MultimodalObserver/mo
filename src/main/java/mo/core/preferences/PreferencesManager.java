package mo.core.preferences;

import com.thoughtworks.xstream.XStream;
import mo.core.utils.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreferencesManager {
    
    public static final Logger LOGGER =  Logger.getLogger(PreferencesManager.class.getName());

    public PreferencesManager() {
    }

    public static void save(Object wrapped, File file) {
        FileOutputStream fos = null;
        XStream x;

        //if (!file.isFile()) {
        try {
            x = new XStream();
            x.processAnnotations(wrapped.getClass());

            fos = new FileOutputStream(file);
            fos.write("<?xml version=\"1.0\"?>\n".getBytes("UTF-8")); //write XML header, as XStream doesn't do that for us
            byte[] bytes = x.toXML(wrapped).getBytes("UTF-8");
            fos.write(bytes);

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                   LOGGER.log(Level.SEVERE, null, ex);
                }
            }
        }
        //} else {
        //    System.out.println("ya existe");
        //}
    }

    public static Object load(Class wrapperClass, File xmlFile) {
        XStream x = new XStream();
        Object o = null;
        x.processAnnotations(wrapperClass);
        try {
            o = x.fromXML(xmlFile);
        } catch (Exception ex) {
            System.out.println("Can't load preferences file");
            //Logger.getLogger(PreferencesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return o;
    }

    public static Object loadOrCreate(Class wrapperClass, File xmlFile) {
        Object o = load(wrapperClass, xmlFile);
        if (null == o) {
            try {
                o = wrapperClass.newInstance();
                if (!xmlFile.isFile()) {
                    if (xmlFile.createNewFile()) {
                        save(o, xmlFile);
                    }
                }
            } catch (InstantiationException | IllegalAccessException | IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return o;
    }

    public static void main(String[] args) {
        PreferencesManager pm = new PreferencesManager();
        //AppPreferencesWrapper a = new AppPreferencesWrapper();
        //a.addOpenedProject("chau");
        //pm.save(a, new File(Utils.getBaseFolder()+"/app.xml"));

        AppPreferencesWrapper aa;

        aa = (AppPreferencesWrapper) pm.load(AppPreferencesWrapper.class,
                new File(Utils.getBaseFolder() + "/app.xml"));

        if (aa != null) {
            aa.getOpenedProjects().stream().forEach((openedProject) -> {
                System.out.println(openedProject.getLocation());
            });

        }
    }
}
