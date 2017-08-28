package mo.core.plugin.pluginmanagement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import mo.core.plugin.Dependency;
import mo.core.plugin.IPluginsObserver;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;

/**
 *
 * @author felo
 */


class PluginInfo extends JPanel {
    
    private final Plugin plugin;    
    
    private final PluginList treeList;
    
    private JPanel getInformationTab(){    
        
        TupleList tuples = new TupleList();
        
        tuples.addTuple("Name", plugin.getName());
        
        if(plugin.isThirdParty()) 
            tuples.addTuple("Author", "xyz");
        
        if(plugin.isThirdParty()) 
            tuples.addTuple("Website", new JButton("Open Website"));
        
        tuples.addTuple("Version", plugin.getVersion());
        tuples.addTuple("Id", plugin.getId());
        
        if(plugin.isThirdParty()) 
            tuples.addTuple("Path", plugin.getPath());
        
        tuples.addTuple("Source", plugin.isThirdParty()? "Third party" : "Built-in in MO");
        tuples.addScrollText("Description", plugin.getDescription());          
        
       
        return tuples;
   
    }   
    
    
    
    private JPanel getOperations(){
        
        TupleList tuples = new TupleList();
        
        JButton uninstallBtn = new JButton("Uninstall");
        
        uninstallBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!plugin.isThirdParty())
                    return;

       
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
                
                treeList.refresh();
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
    

    public PluginInfo(Plugin p, PluginList treeList) {
        
        this.treeList = treeList;
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
        
        
        if(plugin.isThirdParty()){
            tabbedPane.addTab("Operations", getOperations());
        }
        
        tabbedPane.addTab("Advanced", getAdvanced());
        
	

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);

        add(tabbedPane, BorderLayout.CENTER);
        
        
        
        

    }


}








class PluginError extends JPanel{
    
    
    
    public PluginError(Plugin plugin, PluginList treeList){
        
        
        
        JLabel pluginTitle = new JLabel(plugin.getName(), SwingConstants.LEFT);
        pluginTitle.setFont(new Font("", Font.BOLD, 20));
        Dimension d = pluginTitle.getPreferredSize();
        d.height = 25;
        pluginTitle.setPreferredSize(d); 
        
        JLabel pluginVersion = new JLabel("v" + plugin.getVersion(), SwingConstants.LEFT);
        JPanel top = new JPanel();
        

        top.add(pluginTitle);
        top.add(pluginVersion);        
        
        
        
        JPanel content = new JPanel();
        
        String errorMsg = "Plugin " + plugin.getName() + " (" + plugin.getId() + ") is corrupted.";
        
        content.add(new JLabel(errorMsg));        
        
        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);        
        add(content, BorderLayout.CENTER);

        
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



public class PluginList extends JSplitPane implements IPluginsObserver {
 
    
    private Plugin focusedPlugin = null;
    
    public void refresh(){    
        showList();
        if(focusedPlugin != null && !PluginRegistry.getInstance().getPluginData().pluginIsRegistered(focusedPlugin)){
            this.setRightComponent(new JPanel());
        }
    }
    
    public PluginList(){
        
        super(JSplitPane.HORIZONTAL_SPLIT, new JPanel(), new JPanel());      
        
        PluginRegistry.getInstance().subscribePluginsChanges(this);

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

        this.setRightComponent(new PluginInfo(plugin, this));        
        
    }
    
    
    private void showPluginError(Plugin plugin){
        
        this.setRightComponent(new PluginError(plugin, this));
        
    }
    
    
    private void showList(){
        
        List<Plugin> plugins = PluginRegistry.getInstance().getPluginData().getPlugins();
        
        
        DefaultMutableTreeNode allPlugins = new DefaultMutableTreeNode("Installed Plugins");
        DefaultMutableTreeNode hardCodedPlugins = new DefaultMutableTreeNode("MO");
        DefaultMutableTreeNode dynamicPlugins = new DefaultMutableTreeNode("Third party");
        
        for(Plugin p : plugins){
            
            TreeNode node = new TreeNode(p);
           
            if(p.isThirdParty()){
               dynamicPlugins.add(node);
            } else {
                hardCodedPlugins.add(node);
            }                     
        }
        
        JTree tree = new JTree(allPlugins);
        JScrollPane scroll = new JScrollPane(tree);
        
        
        tree.addTreeSelectionListener(new TreeSelectionListener(){
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                
                
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
                if (node == null || !(node instanceof TreeNode)) return;
                TreeNode nodeData = (TreeNode) node;
                
                focusedPlugin = nodeData.getPlugin();
                
                if(nodeData.getPlugin().sanityCheck()){
                    
                    
                    showPluginInfo(focusedPlugin);
                    
                    
                } else {
                    
                    showPluginError(focusedPlugin);
                    
                }
                
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

    @Override
    public void update() {
        refresh();
        
    }
    
}
