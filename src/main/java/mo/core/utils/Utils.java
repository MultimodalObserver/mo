package mo.core.utils;

import java.io.File;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class Utils {
    public static String getBaseFolder(){
        File baseFolder = 
                new File(
                        Utils.class.getProtectionDomain()
                                .getCodeSource().getLocation().getPath());
        baseFolder = new File(baseFolder.getParent());
        if (baseFolder.getPath().endsWith("classes")){
            baseFolder = new File (baseFolder.getParent());
        }
        return baseFolder.getPath();
    }
}
