package core;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainWindow extends JFrame implements MainWindowI {
    private JMenuBar menuBar;
    private JMenu modulesMenu;
    private Collection<String> modulesList; 
    private Collection<JMenuItem> modulesMenuItems;

    public MainWindow() {
        modulesMenuItems = new ArrayList<>();
        modulesMenu = new JMenu("Modules");
    }
    
    public void createAndShowGUI(){
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("memoria");
        setLocation(450, 200);
        setPreferredSize(new Dimension(300, 300));
        menuBar = new JMenuBar();
        for (JMenuItem modulesMenuItem : modulesMenuItems) {
            modulesMenu.add(modulesMenuItem);
        }
        
        menuBar.add(modulesMenu);

        setJMenuBar(menuBar);
        setVisible(true);
        
        pack();
    }
    
    public void addModulesMenuItemsActionListener(ActionListener l){
        //System.out.println("add");
        for (JMenuItem i : modulesMenuItems) {
            //System.out.println(i);
            i.addActionListener(l);
        }
    }
    
    @Override
    public void setAvaibleModules(Collection<String> modulesList) {
        if (this.modulesList == null)
            this.modulesList = new ArrayList<>();
        
        this.modulesList.addAll(modulesList);
        
        for (String string : modulesList) {
            JMenuItem i = new JMenuItem(string);
            modulesMenuItems.add(i);
        }
        
    }
}
