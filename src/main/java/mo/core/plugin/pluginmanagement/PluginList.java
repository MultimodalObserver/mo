package mo.core.plugin.pluginmanagement;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;

/**
 *
 * @author felo
 */


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
        
        
        JPanel jp = new JPanel();
        JLabel jl = new JLabel(plugin.getName());
        jp.add(jl);
        this.setRightComponent(jp);
        
        
    }
    
    
    private void showList(){
        
        List<Plugin> plugins = PluginRegistry.getInstance().getPluginData().getPlugins();
        
        
        DefaultMutableTreeNode allPlugins = new DefaultMutableTreeNode("Installed Plugins");
        DefaultMutableTreeNode hardCodedPlugins = new DefaultMutableTreeNode("MO");
        DefaultMutableTreeNode dynamicPlugins = new DefaultMutableTreeNode("Third party");
        
        for(Plugin p : plugins){
            
            TreeNode node = new TreeNode(p);
            //DefaultMutableTreeNode node = new DefaultMutableTreeNode(p.getName());
            
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

        if(!hardCodedPlugins.isLeaf())
            allPlugins.add(hardCodedPlugins);
        
        if(!dynamicPlugins.isLeaf())
            allPlugins.add(dynamicPlugins);       
        
        
        expandTree(tree);
        this.getLeftComponent().revalidate();
        
    }
    
}
