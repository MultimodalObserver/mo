package mo.core;

import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import mo.core.ui.frames.DockableElement;
import mo.core.ui.frames.DockablesRegistry;
import mo.core.ui.menubar.MenuBar;
import mo.filemanagement.FileRegistry;

public class MainWindow extends JFrame {

    public MainWindow() {
        setLookAndFeel();
    }

    public void createAndShowGUI() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Multimodal Observer");

        //fileMenuItems.add(newProject);
//        for (JMenuItem fileMenuItem : fileMenuItems) {
//            fileMenu.add(fileMenuItem);
//        }

        setJMenuBar(MenuBar.getInstance());
        
        DockablesRegistry dr = DockablesRegistry.getInstance();
        dr.setJFrame(this);
        add(dr.getControl().getContentArea());
        
        
        DockableElement filesDock = FileRegistry.getInstance().getFilesPane().getElement();
        dr.getControl().addDockable(filesDock);
        filesDock.setVisible(true);
        
//        JToolBar tools = new JToolBar();
//        tools.add(new JButton("asd"));
//        control.addDockable(tools);

        pack();
        setVisible(true);
    }

    public void addNewProjectListener(ActionListener l) {
        //newProject.addActionListener(l);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainWindow.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }
}
