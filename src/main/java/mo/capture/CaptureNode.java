package mo.capture;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import mo.organization.StageNode;
import mo.organization.visualization.tree.TreeOrganizationStageNode;

public class CaptureNode implements StageNode, TreeOrganizationStageNode {

    JPopupMenu menu;
    DefaultMutableTreeNode node;

    public CaptureNode() {
        menu = new JPopupMenu();
        menu.add(new JMenuItem("hi"));
        
        node = new DefaultMutableTreeNode("Capture");
    }

    @Override
    public JPopupMenu getPopupMenu() {
        return menu;
    }

    @Override
    public boolean mustBeUnique() {
        return true;
    }

    @Override
    public DefaultMutableTreeNode getNode() {
        return node;
    }

    @Override
    public String getStageName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
