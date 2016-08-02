package mo.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.core.utils.Utils;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class DirectoryWatcher {

    private WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private List<WatchHandler> handlers;
    private Thread watcherThread;

    public DirectoryWatcher() {
        this.keys = new HashMap<>();
        handlers = new ArrayList<>();
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            Logger.getLogger(DirectoryWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) {
        try {
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            Path prev = keys.get(key);
//            if (prev == null) {
//                System.out.format("register: %s\n", dir);
//            } else if (!dir.equals(prev)) {
//                System.out.format("update: %s -> %s\n", prev, dir);
//            }

            keys.put(key, dir);
        } catch (IOException ex) {
            Logger.getLogger(DirectoryWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) {
        try {
            // register directory and sub-directories
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(DirectoryWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addDirectory(Path dir, boolean recursive) {
        if (recursive) {
            registerAll(dir);
        } else {
            register(dir);
        }
    }

    public void addWatchHandler(WatchHandler handler) {
        handlers.add(handler);
    }

    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public void start() {
        
        watcherThread = new Thread() {
            @Override
            public void run() {

                    while (!interrupted()) {
                        
                        // wait for key to be signalled
                        WatchKey key;
                        try {
                            key = watcher.take();
                        } catch (InterruptedException x) {
                            return;
                        }

                        Path dir = keys.get(key);
                        if (dir == null) {
                            System.err.println("WatchKey not recognized!!");
                            continue;
                        }

                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind kind = event.kind();

                            // TBD - provide example of how OVERFLOW event is handled
                            if (kind == OVERFLOW) {
                                continue;
                            }

                            // Context for directory entry event is the file name of entry
                            WatchEvent<Path> ev = cast(event);
                            Path name = ev.context();
                            Path child = dir.resolve(name);

                            //System.out.format(">>Name:%s\n>>C:%s\n", name, child);
                            // print out event
                            //System.out.format(">event %s: %s\n", event.kind().name(), child);
                            // if directory is created, and watching recursively, then
                            // register it and its sub-directories
                            if (/*recursive &&*/(kind == ENTRY_CREATE)) {
                                //System.out.format("Create %s\n", child);

                                if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                    registerAll(child);
                                }

                                for (WatchHandler handler : handlers) {
                                    handler.onCreate(child.toFile());
                                }
                            } else if (kind == ENTRY_MODIFY) {
                                //System.out.format("Modify %s\n", child);
                                for (WatchHandler handler : handlers) {
                                    handler.onDelete(child.toFile());
                                }
                            } else if (kind == ENTRY_DELETE) {
                                //System.out.format("Delete %s\n", child);

                                for (WatchHandler handler : handlers) {
                                    handler.onDelete(child.toFile());
                                }
                            }
                        }

                        // reset key and remove from set if directory no longer accessible
                        boolean valid = key.reset();
                        if (!valid) {
                            keys.remove(key);

                            // all directories are inaccessible
                            if (keys.isEmpty()) {
                                break;
                            }
                        }
                    }
                }
            

        };
        watcherThread.start();

    }

    public void stop() {
        if (watcherThread != null && !watcherThread.isInterrupted() 
                && watcherThread.isAlive()) {
            watcherThread.interrupt();
        }
    }
    
    public static void main(String args[]) throws InterruptedException {
        DirectoryWatcher d = new DirectoryWatcher();
        d.addDirectory((new File("F:\\test")).toPath(), true);
        d.addWatchHandler(new WatchHandler() {
            @Override
            public void onCreate(File file) {
                System.out.println("create");
            }

            @Override
            public void onDelete(File file) {

            }

            @Override
            public void onModify(File file) {

            }
        });
        d.start();
        Thread.sleep(15000);
        d.stop();
    }
}
