package mo.core.plugin;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class VersionUtils {
    public static String semverize(String version) {
        String[] v = version.split("\\.");

        if (v.length == 1)
            version += ".0.0";
        else if (v.length == 2)
            version += ".0";
        
        return version;
    }
    public static void main(String [] args) {
        String s = " as.as ";
        System.out.println(s.split("\\.").length);
    }
}
