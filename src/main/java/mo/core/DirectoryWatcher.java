package mo.core;

import java.io.File;
import java.io.IOException;
import static java.lang.Thread.interrupted;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class DirectoryWatcher {
    private static final Logger LOGGER = Logger.getLogger(DirectoryWatcher.class.getName());

    private WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private List<WatchHandler> handlers;
    private Thread watcherThread;
    private AtomicBoolean isPaused;

    public DirectoryWatcher() {
        keys = new HashMap<>();
        handlers = new ArrayList<>();
        isPaused = new AtomicBoolean(false);
        
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void register(Path dir) {
        if (!keys.containsValue(dir)) {
            try {
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                keys.put(key, dir);
                //System.out.println("registered "+key+" "+dir);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void registerAll(final Path start) {
        try {
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void addDirectory(Path dir, boolean recursive) {
        if (recursive) {
            registerAll(dir);
        } else {
            register(dir);
        }
        if (isPaused.get()) {
            start();
            isPaused.set(false);
        }
    }
    
    public void removeDirectory(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (keys.containsValue(dir)) {
                        Set<Map.Entry<WatchKey, Path>> ks =  keys.entrySet();
                        for (Map.Entry<WatchKey, Path> entry : ks) {
                            if (entry.getValue().equals(dir)) {
                                keys.remove(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
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
                            LOGGER.log(Level.SEVERE, null, x);
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
                                //System.out.println("DirectoryWatcher overflow");
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
                            if (kind == ENTRY_CREATE) {
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
                                    handler.onModify(child.toFile());
                                }
                            } else if (kind == ENTRY_DELETE) {
                                //System.out.format("Delete %s\n", child);

                                for (WatchHandler handler : handlers) {
                                    handler.onDelete(child.toFile());
                                }
                            } else {
                                //System.out.println("Other kind");
                            }
                        }

                        // reset key and remove from set if directory no longer accessible
                        boolean valid = key.reset();
                        if (!valid) {
                            //System.out.println("not valid "+ key);
                            key.cancel();
                            keys.remove(key);

                            // all directories are inaccessible
                            if (keys.isEmpty()) {
                                //System.out.println("exiting loop");
                                isPaused.set(true);
                                break;
                            }
                        }
                    }
                    //System.out.println("Exit while");
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
    
    public void addWatchHandler(WatchHandler handler) {
        handlers.add(handler);
    }

    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    public static void main(String args[]) throws InterruptedException {
        DirectoryWatcher d = new DirectoryWatcher();
        d.addDirectory((new File("F:\\test")).toPath(), true);
        d.addWatchHandler(new WatchHandler() {
            @Override
            public void onCreate(File file) {
                System.out.println("created");
            }

            @Override
            public void onDelete(File file) {
                System.out.println("deleted");
            }

            @Override
            public void onModify(File file) {
                System.out.println("modified");
            }
        });
        d.start();
        int res = JOptionPane.showConfirmDialog(new JFrame(), "hola");
        System.exit(0);
    }
}
