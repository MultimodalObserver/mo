package mo.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import mo.core.ui.dockables.DockablesRegistry;
import mo.core.ui.menubar.MenuBar;
import mo.filemanagement.FileRegistry;

public class MainWindow extends JFrame {

    public MainWindow() {
        setLookAndFeel();
    }

    public void createAndShowGUI() {
        
        setTitle("Multimodal Observer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(MenuBar.getInstance());
        
        DockablesRegistry dr = DockablesRegistry.getInstance();
        dr.setJFrame(this);
        add(dr.getControl().getContentArea());

        FileRegistry.getInstance();

        pack();
        setVisible(true);
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
