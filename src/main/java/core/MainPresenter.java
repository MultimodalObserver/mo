package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class MainPresenter {
    private final MainWindow view;
    private final Main main;
    
    public MainPresenter(MainWindow w, Main m){
        this.main = m;
        this.view = w;
    }

    public void start() {
        
        view.setAvaibleModules(main.getModulesClassesList());

        SwingUtilities.invokeLater(view::createAndShowGUI);
        
        view.addModulesMenuItemsActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                moduleSelected(e);
            }

        });
    }
    
    private void moduleSelected(ActionEvent e) {
        //System.out.println(e);
        try {
            try {
                main.InstanciateModule(e.getActionCommand().replace("/", "."));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(MainPresenter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
