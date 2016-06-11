package core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
        
        view.addModulesMenuItemsActionListener((ActionEvent e) -> {
            moduleSelected(e);
        });
        
        view.addNewProjectListener((ActionEvent e) -> {
            System.out.println("hola");
            newProject(e);
        });
    }
    
    private void moduleSelected(ActionEvent e) {
        //System.out.println(e);
        try {
            try {
                main.InstanciateModule(e.getActionCommand().replace("/", "."), view);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(MainPresenter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainPresenter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void newProject(ActionEvent e) {
        WizardDialog w = new WizardDialog(view, "New Project");
        w.addPanel(new NewProjectWizardPanel(w));
        HashMap<String,Object> result = w.showWizard();
        if (result != null){
            System.out.println("mira"+result.get("projectFolder"));
        }
    }
}
