package mo.core.plugin.pluginmanagement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import mo.core.plugin.Dependency;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;

/**
 *
 * @author felo
 */


class PluginInfo extends JPanel {
    
    private final Plugin plugin;    
    
    private JPanel getInformationTab(){    
        
        TupleList tuples = new TupleList();
        
        tuples.addTuple("Name", plugin.getName());
        tuples.addTuple("Author", "xyz");
        tuples.addTuple("Docs", new JButton("Read docs"));
        tuples.addTuple("Version", plugin.getVersion());
        tuples.addTuple("Id", plugin.getId());
        tuples.addTuple("Path", plugin.getPath());
        tuples.addTuple("Third party", plugin.getExternal()? "Yes" : "No");
        tuples.addScrollText("Description", plugin.getDescription());                
        
        return tuples;
   
    }   
    
    
    
    private JPanel getOperations(){
        
        TupleList tuples = new TupleList();
        
        JButton uninstallBtn = new JButton("Uninstall");
        
        uninstallBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!plugin.getExternal())
                    return;
                
                System.out.println("Me falta probar bien todos los flujos alternativos de este metodo");
                System.out.println("* Probar cambiando el archivo mientras el dialogo de confirmacion esta abierto");
                System.out.println("* Probar teniendo varias versiones del mismo plugin y ver que se borre la correcta");
                System.out.println("* Probar que ocurre si un plugin no esta vinculado con ningun archivo");
                System.out.println("* Probar borrando un plugin que borre manualmente desde la carpeta (en otras palabras ya no existe)");
                System.out.println("**** con respecto a lo anterior, se deberia poder actualizar la lista automaticamente en caso que eso pase");
                System.out.println("**** es decir, hacer que el watcher mande una senal para actualizar la lista");
       
                int dialogResult = JOptionPane.showConfirmDialog (null, "Remove " + plugin.getName() + " permanently?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(dialogResult == JOptionPane.YES_OPTION){                                 
                    
                    System.out.println("ACA HAY QUE VER SI EL PLUGIN ESTA SIENDO USADO");
                    if(false){                        
                        JOptionPane.showMessageDialog(null, "Plugin is being used. Please stop recording data and try again.", "Error", JOptionPane.ERROR_MESSAGE);                    
                    }
                    
                    boolean success = PluginRegistry.getInstance().uninstallPlugin(plugin);
                    
                    if(success){
                        
                        System.out.println("Update plugin tree");
                        
                    }
                    else {                        
                        JOptionPane.showMessageDialog(null, "Plugin couldn't be removed.", "Error", JOptionPane.ERROR_MESSAGE);                    
                    }                                       
                    
                }
            }
        });
        
        
        
        tuples.addTuple("Uninstall plugin", uninstallBtn);
        
        
        
        return tuples;
    }
    

    
    private JPanel getAdvanced(){ 
        
        TupleList tuples = new TupleList();
        
        String[] depColumns = {"Id", "Version"};
        String[][] depRows = new String[plugin.getDependencies().size()][];
        
        
        for(int i=0; i<plugin.getDependencies().size(); i++){            
            Dependency dep = plugin.getDependencies().get(i);            
            depRows[i] = new String[]{dep.getId(),dep.getVersion()};
        }
        
        String[] extColumns = {"Name", "Id", "Version"};
        String[][] extRows = new String[plugin.getDependencies().size()][];
        
              
        for(int i=0; i<plugin.getDependencies().size(); i++){            
            Dependency dep = plugin.getDependencies().get(i);
            
            extRows[i] = new String[]{
                dep.getExtensionPoint().getName(),
                dep.getExtensionPoint().getId(),
                dep.getExtensionPoint().getVersion()};
        }

        JTable depTable = new JTable(depRows, depColumns);
        JTable extTable = new JTable(extRows, extColumns);
        
        depTable.setRowSelectionAllowed(false);
        extTable.setRowSelectionAllowed(false);
        
        tuples.addTuple("Dependencies", depTable);
        tuples.addTuple("Extension points", extTable);
                              
        return tuples;        
    }
    

    public PluginInfo(Plugin p) {
        
        this.plugin = p;

        JLabel pluginTitle = new JLabel(plugin.getName(), SwingConstants.LEFT);
        pluginTitle.setFont(new Font("", Font.BOLD, 20));
        Dimension d = pluginTitle.getPreferredSize();
        d.height = 25;
        pluginTitle.setPreferredSize(d); 
        
        JLabel pluginVersion = new JLabel("v" + plugin.getVersion(), SwingConstants.LEFT);
        JPanel top = new JPanel();
        

        top.add(pluginTitle);
        top.add(pluginVersion);        
        
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Information", getInformationTab());
        
        
        if(plugin.getExternal()){
            tabbedPane.addTab("Operations", getOperations());
        }
        
        tabbedPane.addTab("Advanced", getAdvanced());
        
	

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);

        add(tabbedPane, BorderLayout.CENTER);
        
        

    }


}



















































class TreeNode extends DefaultMutableTreeNode{
    
    private Plugin plugin;
    
    TreeNode(Plugin plugin){
        super(plugin.getName());
        this.plugin = plugin;
    }
    
    public Plugin getPlugin(){
        return this.plugin;
    }
}



public class PluginList extends JSplitPane {
 
    
    public void refresh(){    
        showList();
    }
    
    public PluginList(){
        
        super(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel());        

        showList();
        
    }
    
    
    private void expandTree(JTree tree){        
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }        
    }
    
    
    private void showPluginInfo(Plugin plugin_){
        
        // check if plugin actually exists
        List<Plugin> plugins = PluginRegistry.getInstance().getPluginData().getPlugins();
        Plugin plugin = null;
        for(Plugin p : plugins){
            if(p.equals(plugin_)){
                plugin = p;
                break;
            }
        }
        
        

        this.setRightComponent(new PluginInfo(plugin));
        
        
    }
    
    
    private void showList(){
        
        List<Plugin> plugins = PluginRegistry.getInstance().getPluginData().getPlugins();
        
        
        DefaultMutableTreeNode allPlugins = new DefaultMutableTreeNode("Installed Plugins");
        DefaultMutableTreeNode hardCodedPlugins = new DefaultMutableTreeNode("MO");
        DefaultMutableTreeNode dynamicPlugins = new DefaultMutableTreeNode("Third party");
        
        for(Plugin p : plugins){
            
            TreeNode node = new TreeNode(p);
           
            if(p.getExternal()){
               dynamicPlugins.add(node);
            } else {
                hardCodedPlugins.add(node);
            }                     
        }
        
        JTree tree = new JTree(allPlugins);
        JScrollPane scroll = new JScrollPane(tree); 
        
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                       tree.getLastSelectedPathComponent();
                if (node == null || !(node instanceof TreeNode)) return;
                TreeNode nodeData = (TreeNode) node;
                showPluginInfo(nodeData.getPlugin());
            }
        });
        
        this.setLeftComponent(scroll);
        
        if(!dynamicPlugins.isLeaf())
            allPlugins.add(dynamicPlugins); 

        if(!hardCodedPlugins.isLeaf())
            allPlugins.add(hardCodedPlugins);
        
        
        expandTree(tree);
        this.getLeftComponent().revalidate();
        
    }
    
}
