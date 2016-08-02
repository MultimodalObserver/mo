package mo.core;

import java.io.File;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public interface WatchHandler {
    public void onCreate(File file);
    public void onDelete(File file);
    public void onModify(File file);
}
