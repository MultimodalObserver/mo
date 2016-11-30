package mo.organization.visualization.tree;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.tree.*;
import mo.core.I18n;
import mo.core.plugin.Plugin;
import mo.core.plugin.PluginRegistry;
import mo.core.ui.Utils;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.StorableDockable;
import mo.organization.*;

public class OrganizationDockable extends DockableElement implements StorableDockable {

    private static final Logger LOGGER = Logger.getLogger(OrganizationDockable.class.getName());

    private String projectPath;

    private List<DefaultMutableTreeNode> stageNodes;

    private ProjectOrganization organization;

    private JTree tree;

    private I18n i18n;

    private JPopupMenu participantMenu;

    public OrganizationDockable() {

    }

    public OrganizationDockable(ProjectOrganization org) {
        organization = org;
        stageNodes = new ArrayList<>();
        i18n = new I18n(OrganizationDockable.class);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project ");

        tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setCellRenderer(new OrganizationCellRenderer());
        tree.setRowHeight(20);

        DefaultMutableTreeNode participantsNode
                = new DefaultMutableTreeNode(i18n.s("OrganizationDockable.participants"));
        insertNodeInParent(root, participantsNode);

        for (Participant participant : organization.getParticipants()) {
            insertNodeInParent(participantsNode, participant);
        }

        for (Stage s : organization.getStages()) {

            DefaultMutableTreeNode newStageNode = new DefaultMutableTreeNode(s);
            insertNodeInParent(root, newStageNode);
            stageNodes.add(newStageNode);

            for (StagePlugin plugin : s.getPlugins()) {
                for (Configuration configuration : plugin.getConfigurations()) {
                    PluginConfigPair pair = new PluginConfigPair(plugin, configuration);
                    insertNodeInParent(newStageNode, pair);
                }
            }
        }

        JMenuItem addParticipantMenu
                = new JMenuItem(i18n.s("OrganizationDockable.addParticipant"));
        addParticipantMenu.addActionListener((ActionEvent e) -> {
            ParticipantDialog dialog = new ParticipantDialog(organization);
            Participant participant = dialog.showDialog();
            if (participant != null) {
                organization.addParticipant(participant);
                organization.store();
                insertNodeInParent(participantsNode, participant);
            }
        });

        JPopupMenu participantsMenu = new JPopupMenu();
        participantsMenu.add(addParticipantMenu);

        JMenu addStage = new JMenu(i18n.s("OrganizationDockable.addStage"));
        List<Plugin> stagePlugins
                = PluginRegistry.getInstance().getPluginsFor("mo.organization.Stage");
        for (Plugin stagePlugin : stagePlugins) {
            Stage nodeProvider = (Stage) stagePlugin.getNewInstance();
            JMenuItem item = new JMenuItem(nodeProvider.getName());
            item.addActionListener((ActionEvent e) -> {
                addStageNodeIfNotExists((Stage) nodeProvider);
            });
            addStage.add(item);
        }

        JPopupMenu projectMenu = new JPopupMenu();
        projectMenu.add(addStage);

        JMenuItem viewOrEditParticipant
                = new JMenuItem(i18n.s("OrganizationDockable.viewEditParticipant"));
        viewOrEditParticipant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editParticipant();
            }
        });

        JMenuItem deleteParticipant
                = new JMenuItem(i18n.s("OrganizationDockable.deleteParticipant"));
        deleteParticipant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DefaultMutableTreeNode selected
                        = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                Participant p = (Participant) selected.getUserObject();

                String message = i18n.s(
                        "OrganizationDockable.participantDeletionMesssage",
                        p.id);

                Object[] options = {
                    i18n.s("OrganizationDockable.participantDeletionYes"),
                    i18n.s("OrganizationDockable.participantDeletionNo"),
                    i18n.s("OrganizationDockable.participantDeletionCancel")};

                int r = JOptionPane.showOptionDialog(
                        null,
                        message,
                        i18n.s("OrganizationDockable.participantDeletionTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[2]);

                if (r == JOptionPane.YES_OPTION) {
                    //TODO
                } else if (r == JOptionPane.NO_OPTION) {
                    organization.deleteParticipant(p);
                    organization.store();

                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selected.getParent();
                    int index = parent.getIndex(selected);
                    selected.removeFromParent();
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                    model.nodesWereRemoved(parent, new int[]{index}, new Object[]{selected});
                }
            }
        });

        JMenuItem lockItem = new JMenuItem(i18n.s("OrganizationDockable.lock"));
        lockItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode selected
                        = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                Participant p = (Participant) selected.getUserObject();

                if (p.isLocked) {
                    p.isLocked = false;
                } else {
                    p.isLocked = true;
                }

                organization.updateParticipant(p);
                organization.store();

                if (lockItem.getText().equals(i18n.s("OrganizationDockable.lock"))) {
                    lockItem.setText(i18n.s("OrganizationDockable.unlock"));
                } else {
                    lockItem.setText(i18n.s("OrganizationDockable.lock"));
                }

                selected.setUserObject(p);
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.nodeChanged(selected);

            }
        });

        participantMenu = new JPopupMenu();
        participantMenu.add(viewOrEditParticipant);
        participantMenu.add(deleteParticipant);
        participantMenu.add(new JSeparator());
        participantMenu.add(lockItem);

        // TODO add actions menu items dynamically
        for (Stage stage : organization.getStages()) {
            addStageMenuToParticipantMenu(stage);
        }

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event) && event.isPopupTrigger()) {

                    JComponent source = (JComponent) event.getSource();
                    int x = event.getX();
                    int y = event.getY();

                    int row = tree.getRowForLocation(event.getX(), event.getY());

                    tree.setSelectionRow(row);
                    DefaultMutableTreeNode selected
                            = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                    if (row == -1) {
                        projectMenu.show(source, x, y);
                    } else if (selected.equals(participantsNode)) {
                        participantsMenu.show(source, x, y);
                    } else if (selected.equals(root)) {
                        projectMenu.show(source, x, y);
                    } else if (selected.getUserObject() instanceof Participant) {
                        Participant p = (Participant) selected.getUserObject();
                        if (p.isLocked) {
                            lockItem.setText(i18n.s("OrganizationDockable.unlock"));
                        } else {
                            lockItem.setText(i18n.s("OrganizationDockable.lock"));
                        }
                        participantMenu.show(source, x, y);
                    } else if (selected.getUserObject() instanceof Stage) {
                        JPopupMenu menu = new JPopupMenu("stage");
                        Stage s = (Stage) selected.getUserObject();
                        for (StagePlugin sp : s.getPlugins()) {
                            JMenuItem item = new JMenuItem(sp.getName());
                            item.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    Configuration c = sp.initNewConfiguration(organization);
                                    if (c != null) {
                                        insertNodeInParent(
                                                selected, new PluginConfigPair(sp, c));
                                        organization.store();
                                    }
                                }
                            });
                            menu.add(item);
                        }
                        menu.show(source, x, y);
                    } else if (selected.getUserObject() instanceof PluginConfigPair) {
                        JPopupMenu menu = new JPopupMenu("Config");

                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2) {
                    int row = tree.getRowForLocation(event.getX(), event.getY());
                    if (row == -1) {
                        return;
                    }
                    tree.setSelectionRow(row);
                    DefaultMutableTreeNode selected
                            = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                    if (selected.getUserObject() instanceof Participant) {
                        editParticipant();
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tree);
        add(scroll);
    }

    private void insertNodeInParent(DefaultMutableTreeNode parent, Object userObjectToInsert) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(userObjectToInsert);
        insertNodeInParent(parent, newNode);
    }

    private void insertNodeInParent(DefaultMutableTreeNode parent, DefaultMutableTreeNode node) {
        parent.add(node);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.nodesWereInserted(parent, new int[]{model.getIndexOfChild(parent, node)});
        TreePath p = new TreePath(model.getPathToRoot(node));
        tree.expandPath(p.getParentPath());
    }

    private void editParticipant() {

        DefaultMutableTreeNode selectedNode
                = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (selectedNode.getUserObject() != null
                && selectedNode.getUserObject() instanceof Participant) {

            Participant selected = (Participant) selectedNode.getUserObject();
            ParticipantDialog dialog = new ParticipantDialog(organization, selected);
            Participant editedParticipant = dialog.showDialog();

            if (editedParticipant != null) {

                organization.updateParticipant(editedParticipant);
                organization.store();

                selectedNode.setUserObject(editedParticipant);
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.nodeChanged(selectedNode);
            }
        }
    }

    private void addStageNodeIfNotExists(Stage stage) {
        String newNodeName = stage.getName();

        for (DefaultMutableTreeNode node : stageNodes) {

            if (node.getUserObject() instanceof Stage) {
                String name = ((Stage) node.getUserObject()).getName();
                if (name.equals(newNodeName)) {
                    System.out.println("Node already exists");
                    return;
                }
            }
        }
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(stage);
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        root.add(newNode);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.nodesWereInserted(root, new int[]{root.getIndex(newNode)});

        stage.setOrganization(organization);

        organization.addStage(stage);
        organization.store();

        stageNodes.add(newNode);

        addStageMenuToParticipantMenu(stage);
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

                XElement e = XIO.readUTF(in);
                String projectFolder = file.getParentFile().getAbsolutePath();
                ProjectOrganization po = new ProjectOrganization(projectFolder);
                OrganizationDockable d = new OrganizationDockable(po);
                d.setTitleText(e.getElement("title").getString());
                d.setProjectPath(projectFolder);

                return d;
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(file);
        return null;
    }

    List<Participant> getParticipants() {
        return this.organization.getParticipants();
    }

    private void addStageMenuToParticipantMenu(Stage stage) {
        JMenu stageActions = new JMenu(stage.getName());
        for (StageAction action : stage.getActions()) {
            JMenuItem actionItem = new JMenuItem(action.getName());
            actionItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultMutableTreeNode selected
                            = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    Participant p = (Participant) selected.getUserObject();
                    action.init(organization, p, stage);
                }
            });
            stageActions.add(actionItem);
        }
        participantMenu.add(stageActions);
    }

    private class PluginConfigPair {

        StagePlugin plugin;
        Configuration configuration;

        public PluginConfigPair(StagePlugin plugin, Configuration configuration) {
            this.plugin = plugin;
            this.configuration = configuration;
        }

    }

    private static class OrganizationCellRenderer implements TreeCellRenderer {

        private final DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultTreeCellRenderer returnValue
                    = (DefaultTreeCellRenderer) defaultRenderer.
                    getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            I18n i18n = new I18n(OrganizationDockable.class);
            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObject instanceof Participant) {
                    returnValue = getRendererForParticipant(returnValue, (Participant) userObject);
                } else if (userObject instanceof String && ((String) userObject).equals(i18n.s("OrganizationDockable.participants"))) {
                    returnValue = getRendererForParticipants(returnValue);
                } else if (userObject instanceof Stage) {
                    returnValue = getRendererForStage(returnValue, (Stage) userObject);
                } else if (userObject instanceof PluginConfigPair) {
                    returnValue = getRendererForConfig(returnValue, (PluginConfigPair) userObject);
                }
            }
            return returnValue;
        }

        private DefaultTreeCellRenderer getRendererForParticipant(DefaultTreeCellRenderer c, Participant p) {
            if (p.isLocked) {
                c.setIcon(Utils.createImageIcon("images/locked.png", getClass()));
            } else {
                c.setIcon(Utils.createImageIcon("images/unlocked.png", getClass()));
            }
            c.setText(p.id + " " + p.name);
            return c;
        }

        private DefaultTreeCellRenderer getRendererForParticipants(DefaultTreeCellRenderer c) {
            c.setIcon(Utils.createImageIcon("images/participants.png", getClass()));
            return c;
        }

        private DefaultTreeCellRenderer getRendererForStage(DefaultTreeCellRenderer c, Stage s) {
            c.setText(s.getName());
            return c;
        }

        private DefaultTreeCellRenderer getRendererForConfig(
                DefaultTreeCellRenderer c, PluginConfigPair p) {

            c.setText(p.plugin.getName() + " (" + p.configuration.getId() + ")");
            return c;
        }
    }
}
