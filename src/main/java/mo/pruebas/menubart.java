package mo.pruebas;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class menubart {
    public static void main(String[] args){
        JFrame f = new JFrame("asd");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setSize(300, 300);
        
        
        
        JMenu menu = new JMenu("sad");
        
        JMenu menu2 = new JMenu("menu2");
        
        JMenu item = new JMenu("dsa");
        
        item.add(new JMenuItem("dasd"));
        
        
        menu.add(item);
        //menu.add
        
        JMenuBar menubar = new JMenuBar();
        menubar.add(menu2);
        menubar.add(menu);
        menubar.add(new JMenuItem("holas"));
        
        for (MenuElement e : menubar.getSubElements()) {
            System.out.println(e.getComponent()+" "+menubar.getComponentZOrder(e.getComponent()));
        }
        
        f.setJMenuBar(menubar);
        f.setVisible(true);
    }
}
