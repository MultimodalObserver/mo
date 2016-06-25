package mo.core;

import mo.core.Utils;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Celso
 */
public class FilesTreeModel implements TreeModel {

    private final File root = new File("multimodal-observer");
    private final HashSet listeners;
    private TreeSet<File> files;
    
    public FilesTreeModel(){
        listeners = new HashSet<>();
        files = new TreeSet<>();
    }

    public void addFile(File f){
        files.add(f);
    }
    
    public void removeFile(File f){
        File toRemove = null;
        
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
            File next = iterator.next();
            if (f.getAbsolutePath().compareTo(next.getAbsolutePath())==0)
                toRemove = next;
        }
        
        if (null != toRemove)
            files.remove(toRemove);
    }
    
    public void setFiles(TreeSet<File> files){
        this.files = files;
    }
    
    public TreeSet<File> getFiles(){
        return files;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent.equals(root)){
            int i = 0;
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                Object next = iterator.next();
                if (i==index) return next;
                i++;
            }
        }
        File p = (File) parent;
        String[] list = p.list();
        return new File(p, list[index]);
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent.equals(root))
            return files.size();
        
        File p = (File) parent;
        return p.isFile() ? 0 : p.list().length;
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node.equals(root) )
            return files.isEmpty();
        
        return ((File) node).isFile();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent.equals(root)){
            int i = 0;
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                Object next = iterator.next();
                if (next.equals(child)) return i;
                i++;
            }
        }
        
        File p = (File) parent;
        File c = (File) child;
        String[] list = p.list();
        
        for (int i = 0; i < list.length; i++) {
            if (c.getName().compareTo(list[i]) == 0) return i;
        }
        
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        if (null != l && !listeners.contains(l))
            listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        if (null != l && listeners.contains(l))
            listeners.remove(l);
    }
    
    public static void main(String[] args) throws InterruptedException{
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        FilesTreeModel m = new FilesTreeModel();
        m.addFile(new File(Utils.getBaseFolder()));
        
        JTree tree = new JTree(m){
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                return super.convertValueToText(((File)value).getName(), selected, expanded, leaf, row, hasFocus); //To change body of generated methods, choose Tools | Templates.
            }
            
        };
        tree.setRootVisible(false);
        JScrollPane scrollPane = new JScrollPane(tree);
        frame.add(scrollPane);

        frame.setSize(300, 200);
        frame.setVisible(true);
        
        
        //Thread.sleep(5000);
        
        m.addFile(new File("C:/Users/Celso/Desktop/pruebas/watchDir"));
        tree.updateUI();
        
        m.removeFile(new File(Utils.getBaseFolder()));
        tree.updateUI();
    }
}
