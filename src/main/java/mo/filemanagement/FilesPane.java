package mo.filemanagement;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;
import mo.core.ui.frames.DockableElement;
import mo.core.ui.frames.DockablesRegistry;
import mo.core.ui.frames.IDockableElement;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
@Extension(
        xtends = {
            @Extends(extensionPointId = "mo.core.ui.frames.IDockableElement")
        }
)
public class FilesPane implements IDockableElement {

    private DockableElement dockable;
    private final FilesTreeModel filesTreeModel;
    private final JTree filesTree;

    public FilesPane() {

        dockable = new DockableElement("Files", "Files");
        filesTreeModel = new FilesTreeModel();

        JPopupMenu popup = new JPopupMenu();
        JMenuItem mi = new JMenuItem("Insert a children");
        //mi.addActionListener(this);
        mi.setActionCommand("insert");
        popup.add(mi);
        mi = new JMenuItem("Remove this node");
        //mi.addActionListener(this);
        mi.setActionCommand("remove");
        popup.add(mi);
        popup.setOpaque(true);
        popup.setLightWeightPopupEnabled(true);

        filesTree = new JTree(filesTreeModel) {
            @Override
            public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                return super.convertValueToText(((File) value).getName(), selected, expanded, leaf, row, hasFocus); //To change body of generated methods, choose Tools | Templates.
            }

        };

        filesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        filesTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                // thanks to urbanq for the bug fix!
                
                if (SwingUtilities.isRightMouseButton(event)) {
                    System.out.println("pressed");
                    int row = filesTree.getRowForLocation(event.getX(), event.getY());
                    if (row == -1) {
                        return;
                    }
                    filesTree.setSelectionRow(row);
                    File selected = (File) filesTree.getLastSelectedPathComponent();
                    System.out.println(selected);
                    if (event.isPopupTrigger()) {
                        
                        JPopupMenu pop = PopupRegistry.getInstance().getPopupFor(selected);
                        
                        if (pop != null)
                        pop.show((JComponent) event.getSource(),
                                event.getX(), event.getY());
                        //popup.show((JComponent) event.getSource(), event.getX(), event.getY());
                    }
                }
            }
            
        });
        JScrollPane filesScrollPane = new JScrollPane(filesTree);

        //TODO why is not working when there are no files to show
        //filesTree.setRootVisible(false);
        filesTree.setShowsRootHandles(true);

        //p.setLayout(new GridBagLayout());
        //GridBConstraints c = new GridBConstraints();
        //p.add(new JTree(),c.clear().f(GridBConstraints.BOTH).wx(1).wy(1));
        //p.add();
        dockable.add(filesScrollPane);

        //dockable.setVisible(true);
    }

    @Override
    public DockableElement getElement() {
        return dockable;
    }

    void refreshFiles() {
        //Collection<String> files = FileRegistry.getInstance().getOpenedFiles().keySet();
        //for (String file : files) {
        //System.out.println(file);
        //filesTreeModel.addFile(new File(file));
        //}

        //filesTree.updateUI();
    }

    public JTree getFilesTree() {
        return filesTree;
    }

    public FilesTreeModel getFilesTreeModel() {
        return filesTreeModel;
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("hola");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        FilesPane p = new FilesPane();
        
        CControl control = new CControl(f);
        control.addDockable(p.getElement());
        f.add(control.getContentArea());
        p.getElement().setVisible(true);
        f.setVisible(true);
    }
}
