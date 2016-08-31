package mo.organization.visualization.tree;

import mo.organization.Participant;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.tree.*;

import mo.core.plugin.Dependency;
import mo.core.plugin.ExtPoint;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.StorableDockable;
import mo.organization.StageNodeProvider;

public class OrganizationDockable extends DockableElement implements StorableDockable {

    private static final Logger LOGGER = Logger.getLogger(OrganizationDockable.class.getName());

    private String projectPath;
    private List<Participant> participants;
    private JTree tree;
    
    private List<DefaultMutableTreeNode> stageNodes;

    public static void printDescendants(TreeNode root, String indent) {
        System.out.println(indent + root);
        Enumeration children = root.children();
        if (children != null) {
            indent += " ";
            while (children.hasMoreElements()) {
                printDescendants((TreeNode) children.nextElement(), indent);
            }
        }
    }

    public OrganizationDockable() {
        participants = new ArrayList<>();
        stageNodes = new ArrayList<>();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project ");
        DefaultMutableTreeNode participantsNode = new DefaultMutableTreeNode("Participants");

        root.add(participantsNode);

        JMenuItem addParticipantMenu = new JMenuItem("add participant");
        addParticipantMenu.addActionListener((ActionEvent e) -> {
            ParticipantDialog dialog = new ParticipantDialog(this);
            Participant participant = dialog.showDialog();
            if (participant != null) {
                participants.add(participant);
                DefaultMutableTreeNode newParticipant = new DefaultMutableTreeNode(participant);

                participantsNode.add(newParticipant);
                DefaultTreeModel m = (DefaultTreeModel) tree.getModel();
                m.nodesWereInserted(participantsNode, new int[]{m.getIndexOfChild(participantsNode, newParticipant)});
                
                tree.expandPath(tree.getSelectionPath());
            }
        });

        JPopupMenu participantsMenu = new JPopupMenu();
        participantsMenu.add(addParticipantMenu);

        JMenuItem visualizationMenu = new JMenuItem("Visualization");
        visualizationMenu.addActionListener((ActionEvent e) -> {
            System.out.println(e);
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Visualization");
            addStageNodeIfNotExists(newNode);
            //printDescendants(root, "");
        });

//        JMenuItem captureMenu = new JMenuItem("Capture");
//        captureMenu.addActionListener((ActionEvent e) -> {
//            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Capture");
//            addStageNodeIfNotExists(newNode);
//        });

        JMenu addStage = new JMenu("Add Project Stage");
        
        List<Plugin> stagePlugins = PluginRegistry.getInstance().getPluginsFor("mo.organization.StageNodeProvider");
        System.out.println(this+" "+stagePlugins.size());
        for (Plugin stagePlugin : stagePlugins) {
            System.out.println("for"+stagePlugin);
            StageNodeProvider nodeProvider = (StageNodeProvider) stagePlugin.getNewInstance();
            JMenuItem item = nodeProvider.getMenuItem();
            item.addActionListener((ActionEvent e) -> {
                addStageNodeIfNotExists(((TreeOrganizationStageNode) nodeProvider.getStageNode()).getNode());
            });
            addStage.add(nodeProvider.getMenuItem());
        }
        
        //addStage.add(captureMenu);
        addStage.add(visualizationMenu);

        JPopupMenu projectMenu = new JPopupMenu();
        projectMenu.add(addStage);

        tree = new JTree(root);
        tree.setCellRenderer(new OrganizationCellRenderer());
        tree.setRowHeight(20);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event) && event.isPopupTrigger()) {
                    int row = tree.getRowForLocation(event.getX(), event.getY());
                    if (row == -1) {
                        return;
                    }
                    tree.setSelectionRow(row);
                    DefaultMutableTreeNode selected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    System.out.println("selected " + selected);
                    if (selected.equals(participantsNode)) {
                        participantsMenu.show((JComponent) event.getSource(), event.getX(), event.getY());
                    } else if (selected.equals(root)) {
                        projectMenu.show((JComponent) event.getSource(), event.getX(), event.getY());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
                    System.out.println("holi");
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tree);
        add(scroll);
    }
    
    private void addStageNodeIfNotExists(DefaultMutableTreeNode stageNode) {
        String newNodeName = "";
        if (stageNode.getUserObject() instanceof String) {
            newNodeName = (String) stageNode.getUserObject();
        } else {
            return;
        }
        
        for (DefaultMutableTreeNode node : stageNodes) {
            
            if (node.getUserObject() instanceof String) {
               String name = (String) node.getUserObject();
               if (name.equals(newNodeName)) {
                   System.out.println("Node already exists");
                   return;
               }
            }
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        root.add(stageNode);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.nodesWereInserted(root, new int[]{root.getIndex(stageNode)});
        //printDescendants(root, "");
        stageNodes.add(stageNode);
    }

    public String getProjectPath() {
        return this.projectPath;
    }

    public void setProjectPath(String path) {
        this.projectPath = path;
    }

    @Override
    public File dockableToFile() {
        try {

            String relativePathToFile = "organization-visualization-tree.xml";
            File file = new File(this.projectPath, relativePathToFile);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }

            file.createNewFile();

            XElement e = new XElement("config");
            XElement b = new XElement("title");
            b.setString(this.getTitleText());
            e.addElement(b);

            XElement p = new XElement("participants");
            for (Participant participant : participants) {
                XElement xParticipant = new XElement("participant");
                xParticipant.addElement("id").setString(participant.id);
                xParticipant.addElement("name").setString(participant.name);
                xParticipant.addElement("notes").setString(participant.notes);
                XElement date = new XElement("date");
                Calendar c = Calendar.getInstance();
                c.setTime(participant.date);
                date.addElement("day").setInt(c.get(Calendar.DAY_OF_MONTH));
                date.addElement("month").setInt(c.get(Calendar.MONTH));
                date.addElement("year").setInt(c.get(Calendar.YEAR));
                xParticipant.addElement(date);
                p.addElement(xParticipant);
            }
            e.addElement(p);
            
            

            XIO.writeUTF(e, new FileOutputStream(file));

            return file;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public DockableElement dockableFromFile(File file) {

        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {

                XElement e = XIO.read(in, "UTF-8");;
                OrganizationDockable d = new OrganizationDockable();
                d.setTitleText(e.getElement("title").getString());
                d.setProjectPath(file.getParentFile().getAbsolutePath());

                XElement[] ps = e.getElement("participants").getElements("participant");
                for (XElement participant : ps) {
                    Participant p = new Participant();
                    p.id = participant.getElement("id").getString();
                    p.name = participant.getElement("name").getString();
                    p.notes = participant.getElement("notes").getString();

                    SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy");
                    String day = participant.getElement("date").getElement("day").getString();
                    String month = participant.getElement("date").getElement("month").getString();
                    String year = participant.getElement("date").getElement("year").getString();
                    Date date = new Date();
                    try {
                        date = formatter.parse(day + " " + month + " " + year);
                    } catch (ParseException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                    p.date = date;
                    d.participants.add(p);

                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) d.tree.getModel().getRoot();
                    DefaultMutableTreeNode parts = (DefaultMutableTreeNode) root.getFirstChild();
                    parts.add(new DefaultMutableTreeNode(p));
                    d.tree.expandRow(2);

                }

                return d;
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(file);
        return null;
    }

    List<Participant> getParticipants() {
        return this.participants;
    }

    private static class OrganizationCellRenderer implements TreeCellRenderer {

        private final DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultTreeCellRenderer returnValue = (DefaultTreeCellRenderer) defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObject instanceof Participant) {
                    returnValue = getTreeCellRendererComponentForParticipant(returnValue, (Participant) userObject);
                } else if (userObject instanceof ExtPoint) {
                    returnValue = getTreeCellRendererComponentForExtPoint(returnValue, (ExtPoint) userObject);
                } else if (userObject instanceof Dependency) {
                    Dependency d = (Dependency) userObject;
                    if (d.isPresent()) {
                        returnValue = getTreeCellRendererComponentForExtPoint(returnValue, d.getExtensionPoint());
                    } else {
                        returnValue = getTreeCellRendererComponentForDependency((DefaultTreeCellRenderer) (new DefaultTreeCellRenderer()).getTreeCellRendererComponent(tree, value, leaf, expanded, leaf, row, hasFocus), d);
                    }
                }
            }
            return returnValue;
        }

        private DefaultTreeCellRenderer getTreeCellRendererComponentForParticipant(DefaultTreeCellRenderer c, Participant o) {
            c.setText(o.id + " " + o.name);
            return c;
        }

        private DefaultTreeCellRenderer getTreeCellRendererComponentForExtPoint(DefaultTreeCellRenderer c, ExtPoint o) {
            c.setText(o.getName());
            return c;
        }

        private DefaultTreeCellRenderer getTreeCellRendererComponentForDependency(DefaultTreeCellRenderer c, Dependency o) {
            c.setForeground(Color.red);
            c.setBackgroundSelectionColor(Color.white);
            c.setText(o.getId());
            return c;
        }
    }
}
