package mo.filemanagement;

import mo.core.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import mo.core.DirectoryWatcher;
import mo.core.WatchHandler;

public class FilesTreeModel implements TreeModel {

    private final File root = new File("multimodal-observer");
    private final HashSet<TreeModelListener> listeners;
    private TreeSet<File> files;
    private final DirectoryWatcher dirWatcher;
    
    //private final JMenuItem item = new JMenuItem("hola");

    public FilesTreeModel() {
        listeners = new HashSet<>();
        files = new TreeSet<>();
        dirWatcher = new DirectoryWatcher();
        dirWatcher.addWatchHandler(new WatchHandler() {
            @Override
            public void onCreate(File file) {
                //System.out.println("create");
                updateTree(file);
                
                List<Object> path = pathToNode(root, file, new ArrayList<>());
                path.remove(path.size() - 1);
                TreeModelEvent event = new TreeModelEvent(
                        file,
                        path.toArray(),
                        new int[]{getIndexOfChild(path.get(path.size() - 1), file)},
                        getChildren(file.getParentFile()));

                notifyAddedToListeners(event);
            }

            @Override
            public void onDelete(File file) {
                //System.out.println("del");
                updateTree(file);
        
                List<Object> path = pathToNode(root, file.getParentFile(), new ArrayList<>());

                TreeModelEvent removeEvent = new TreeModelEvent(
                        this,
                        path.toArray(),
                        new int[]{},
                        new Object[]{file}
                );
                notifyStructureChangedToListeners(removeEvent);
   
                if (files.contains(file)) {
                    files.remove(file);
                }
            }

            @Override
            public void onModify(File file) {
                //System.out.println("mod");
                updateTree(file);
                
                List<Object> path = pathToNode(root, file, new ArrayList<>());
                if (path != null) {
                    //System.out.println("    path no null>"+path);
                    path.remove(path.size() - 1);

                    TreeModelEvent removeEvent = new TreeModelEvent(
                            this,
                            path.toArray(),
                            new int[]{getIndexOfChild(path.get(path.size() - 1), file)},
                            new Object[]{file}
                    );
                    notifyChangedToListeners(removeEvent);
                } else {
                    
                    for (File next : files) {
                        if ( !next.exists() )
                            files.remove(next);
                    }
                    
                    TreeModelEvent removeEvent = new TreeModelEvent(
                            this,
                            new Object[]{root}
                    );
                    notifyStructureChangedToListeners(removeEvent);
                }
            }
        });
        dirWatcher.start();
        /*item.addActionListener((ActionEvent e) -> {
            //System.out.println("files:");
            for (File file : files) {
                //System.out.println("  "+file);
            }
        });*/
    }
    
    private List<Object> pathToNode(File parent, File node, List<Object> path) {
        path.add(parent);
        
        if (parent.equals(root)) {
            if (files.isEmpty())
                return path;
        }

        if (parent.getAbsolutePath().equals(node.getAbsolutePath())) {
            return path;
        }

        if (isLeaf(parent)) {
            return null;
        }

        int count = getChildCount(parent);
        //System.out.println("c "+count);
        for (int i = 0; i < count; i++) {
            ArrayList<Object> pathCopy = new ArrayList<>(path);
            List<Object> p = pathToNode((File) getChild(parent, i), node, pathCopy);
            if (p != null) {
                return p;
            }
        }

        //System.out.println("returning null "+parent+" "+node+" "+path);
        return null;
    }

    private void notifyChangedToListeners(TreeModelEvent eventToNotify) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesChanged(eventToNotify);
        }
    }

    private void updateTree(File file) {
        //System.out.println(file);
    }

    public void addFile(File f) {
        files.add(f);
        TreeModelEvent event = new TreeModelEvent(
                f,
                new Object[]{root},
                new int[]{getIndexOfChild(root, f)},
                new Object[]{f});

        notifyAddedToListeners(event);
        dirWatcher.addDirectory(f.toPath(), true);
    }

    public void notifyAddedToListeners(TreeModelEvent eventToNotify) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesInserted(eventToNotify);
        }
    }

    public void notifyStructureChangedToListeners(TreeModelEvent eventToNotify) {
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(eventToNotify);
        }
    }

    public void removeFile(File f) {
        File toRemove = null;
        TreeModelEvent removeEvent;

        for (File next : files) {
            if (f.getAbsolutePath().compareTo(next.getAbsolutePath()) == 0) {
                toRemove = next;
            }
        }

        if (null != toRemove) {
            removeEvent = new TreeModelEvent(
                    toRemove,
                    new Object[]{root},
                    new int[]{getIndexOfChild(root, f)},
                    new Object[]{f});
            files.remove(toRemove);
            notifyRemovedToListeners(removeEvent);
        }

    }

    public void notifyRemovedToListeners(TreeModelEvent eventToNotify) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(eventToNotify);
        }
    }

    public TreeSet<File> getFiles() {
        return files;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent.equals(root)) {
            int i = 0;
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                Object next = iterator.next();
                if (i == index) {
                    return next;
                }
                i++;
            }
        }
        File p = (File) parent;
        String[] list = p.list();
        return new File(p, list[index]);
    }

    @Override
    public int getChildCount(Object parent) {
        if (null == parent)
            return 0;
        
        if (parent.equals(root)) {
            return files.size();
        }

        File p = (File) parent;
        if (p.isFile())
            return 0;
        
        if (p.list() != null)
            return p.list().length;
        
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node.equals(root)) {
            return files.isEmpty();
        }

        File file = (File) node;

        if (file.isFile()) {
            return true;
        } else if (file.isDirectory()) {
            return file.list().length == 0;
            //return false;
        }

        return true;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        //System.out.println("*** valueForPathChanged : "
        //        + path + " --> " + newValue);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent.equals(root)) {
            int i = 0;
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                Object next = iterator.next();
                if (next.equals(child)) {
                    return i;
                }
                i++;
            }
        }

        File p = (File) parent;
        File c = (File) child;
        String[] list = p.list();

        for (int i = 0; i < list.length; i++) {
            if (c.getName().compareTo(list[i]) == 0) {
                return i;
            }
        }

        return -1;
    }

    private Object[] getChildren(Object parent) {
        if (parent.equals(root)) {
            return files.toArray();
        }

        if (isLeaf(parent)) {
            return new Object[]{};
        }

        return ((File) parent).listFiles();
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        if (null != l && !listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        if (null != l && listeners.contains(l)) {
            listeners.remove(l);
        }
    }
}
