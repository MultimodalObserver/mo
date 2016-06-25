package mo.core;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

public class MainWindow extends JFrame implements MainWindowI {

    private JMenuBar menuBar;
    private JMenu modulesMenu;
    private Collection<String> modulesList;
    private Collection<JMenuItem> modulesMenuItems;
    private Collection<JMenuItem> fileMenuItems;
    private JMenu fileMenu;
    private JMenuItem newProject;
    private TreeMap<String, String> openedFiles;
    private final FilesTreeModel filesTreeModel;
    private DefaultMutableTreeNode filesTreeRoot;
    private JTree filesTree;
    private JMenu windowMenu;
    private JScrollPane filesScrollPane;
    private final JMenu pluginMenu;

    public MainWindow() {
        setLookAndFeel();
        modulesMenuItems = new ArrayList<>();
        fileMenuItems = new ArrayList<>();
        modulesMenu = new JMenu("Modules");
        newProject = new JMenuItem("New Project...");
        windowMenu = new JMenu("Window");
        pluginMenu = new JMenu("Plugins");
        
        //filesTreeRoot = new DefaultMutableTreeNode("Root Node");
        filesTreeModel = new FilesTreeModel();

    }

    public void createAndShowGUI() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Multimodal Observer");
        setLocation(450, 200);
        setPreferredSize(new Dimension(300, 300));

        //newProject = new JMenuItem("New Project...");
        fileMenuItems.add(newProject);

        fileMenu = new JMenu("File");

        for (JMenuItem fileMenuItem : fileMenuItems) {
            fileMenu.add(fileMenuItem);
        }

        for (JMenuItem modulesMenuItem : modulesMenuItems) {
            modulesMenu.add(modulesMenuItem);
        }

        menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(modulesMenu);
        menuBar.add(windowMenu);
        menuBar.add(pluginMenu);

        setJMenuBar(menuBar);
        
       

        CControl control = new CControl(this);
        add(control.getContentArea());

        DefaultSingleCDockable files = new DefaultSingleCDockable("Files", "Files");
        control.addDockable(files);

        
        
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
                return super.convertValueToText( ((File) value).getName() , selected, expanded, leaf, row, hasFocus); //To change body of generated methods, choose Tools | Templates.
            }
            
        };
        
        filesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        filesTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e){
                // thanks to urbanq for the bug fix!
            int row = filesTree.getRowForLocation(e.getX(), e.getY());
            if(row == -1)
              return;              
            filesTree.setSelectionRow(row);
             if ( e.isPopupTrigger()) {
                 popup.show( (JComponent)e.getSource(), 
                                e.getX(), e.getY() );
                 }
             } 
            
        });
        filesScrollPane = new JScrollPane(filesTree);
        filesTree.setRootVisible(false);

        
        //p.setLayout(new GridBagLayout());
        //GridBConstraints c = new GridBConstraints();

        //p.add(new JTree(),c.clear().f(GridBConstraints.BOTH).wx(1).wy(1));
        //p.add();
        files.add(filesScrollPane);

        files.setVisible(true);
        
//        JToolBar tools = new JToolBar();
//        tools.add(new JButton("asd"));
//        control.addDockable(tools);

        pack();
        setVisible(true);
    }

    public void addNewProjectListener(ActionListener l) {
        newProject.addActionListener(l);
    }

    public void addModulesMenuItemsActionListener(ActionListener l) {
        //System.out.println("add");
        for (JMenuItem i : modulesMenuItems) {
            //System.out.println(i);
            i.addActionListener(l);
        }
    }

    @Override
    public void setAvaibleModules(Collection<String> modulesList) {
        if (this.modulesList == null) {
            this.modulesList = new ArrayList<>();
        }

        this.modulesList.addAll(modulesList);

        for (String string : modulesList) {
            JMenuItem i = new JMenuItem(string);
            modulesMenuItems.add(i);
        }

    }

    private void setLookAndFeel() {
        try {
            // Significantly improves the look of the output in
            // terms of the file names returned by FileSystemView!
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void addOpenedProject(Project p) {
        if (openedFiles == null){
            openedFiles = new TreeMap<>();
        }
        
        openedFiles.put(p.getFolder().getAbsolutePath(),p.getFolder().getName());
    }

    public TreeMap<String, String> getOpenedFiles() {
        if (openedFiles == null)
            openedFiles = new TreeMap<>();
        return openedFiles;
    }
    
    

    void refreshFiles() {

        Collection<String> files = getOpenedFiles().keySet();
        for (String file : files) {
            System.out.println(file);
            filesTreeModel.addFile(new File(file));
        }
        //filesTreeModel.reload();
        //filesTree.updateUI();

    }
}
