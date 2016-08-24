package mo.organization;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.StorableDockable;

public class OrganizationDockable extends DockableElement implements StorableDockable {

    private static final Logger LOGGER = Logger.getLogger(OrganizationDockable.class.getName());
    
    private String projectPath;
    private List<Participant> participants;

    public OrganizationDockable() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Project ");
        DefaultMutableTreeNode participantsNode = new DefaultMutableTreeNode("Participants");
        root.add(participantsNode);

        JMenuItem addParticipantMenu = new JMenuItem("add participant");
        addParticipantMenu.addActionListener((ActionEvent e) -> {
            ParticipantDialog dialog = new ParticipantDialog();
            Participant participant = dialog.showDialog();
            if (participant != null) {
                System.out.println("yess");
            } else {
                System.out.println("nouu");
            }
        });

        JPopupMenu participantsMenu = new JPopupMenu();
        participantsMenu.add(addParticipantMenu);

        JTree tree = new JTree(root);
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
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tree);
        add(scroll);
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
            //System.out.println("this.projectPath "+this.projectPath);
            String relativePathToFile = "organization/config.xml";
            File file = new File(this.projectPath, relativePathToFile);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdir();
            }
            //System.out.println(file);
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
                //System.out.println(file);
                XElement e = XIO.read(in, "UTF-8");
                //System.out.println(e);
                for (XElement xElement : e.children()) {
                    //System.out.println(xElement);
                }
                
                OrganizationDockable d = new OrganizationDockable();
                d.setTitleText(e.getElement("title").getString());
                d.setProjectPath(file.getParentFile().getParentFile().getAbsolutePath());
                return d;
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(file);
        return null;
    }
}
