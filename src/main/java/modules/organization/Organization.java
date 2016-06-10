package modules.organization;

import core.ClassContainer;
import core.ClassesFinder;
import core.Extension;
import core.Modulable;
import core.Plugable;
import core.PluginSelectionDialog;
import java.awt.Dialog;
import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

@Extension(
        codeName = "Organization",
        description = "A organization module",
        name = "Organization",
        packageName = "modules.organization",
        fullName = "modules.organization.Organization",
        interfacesThatImplements = {"core.Modulable"},
        interfacesThatProvide = {"OrganizationModulable"}
)
public class Organization implements Modulable {

    JFrame parentFrame;
    HashSet<ClassContainer> plugins;

    @Override
    public String getName() {
        return "Organization";
    }

    @Override
    public String getDescription() {
        return "A files and project organization module.";
    }

    @Override
    public void init() {

        plugins = new HashSet<>();
        
        ClassesFinder cf = new ClassesFinder();
        for (String moduleExtensionInterface : getModuleExtensionInterfaces()) {
            cf.addInterfaceToFilter(moduleExtensionInterface);
        }
        File f = new File(".");
        cf.addPathSource(f.getAbsolutePath());
        cf.search();
        
        ClassLoader cl = new ClassLoader() {};
        Class<?> clazz;
        
        for (String string : cf.getFullNameClassesFounded()) {
            System.out.println(string);
            ClassContainer cc = new ClassContainer(string);
            try {
                clazz = cl.loadClass(string);
                
                if (clazz.isAnnotationPresent(Extension.class)){
                    // Retrieve all annotations from the class
                    Extension annotation = clazz.getAnnotation(Extension.class);
                    cc.setDescription(annotation.description());
                    cc.setName(annotation.name());
                    cc.setPackageName(annotation.packageName());
                    HashSet<String> interfcsImp = new HashSet();
                    for (String i : annotation.interfacesThatImplements()) {
                        interfcsImp.add(i);
                    }
                    cc.setInterfacesThatImplements(interfcsImp);
                    HashSet<String> interfcsPr = new HashSet();
                    for (String i : annotation.interfacesThatProvide()) {
                        interfcsPr.add(i);
                    }
                    cc.setInterfacesThatProvide(interfcsPr);
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Organization.class.getName()).log(Level.SEVERE, null, ex);
            }
            plugins.add(cc);
        }
        PluginSelectionDialog d = new PluginSelectionDialog(parentFrame, plugins);
        ClassContainer result = d.showDialog();
        if (result == null){
            System.out.println("era null");
        } else {
            try {
                Class<?> pluginClass = Class.forName(result.getFullClassName());
                try {
                    Plugable p = (Plugable) pluginClass.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    Logger.getLogger(Organization.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Organization.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
//d.setVisible(true);
        //JOptionPane.showMessageDialog(null,"Eggs are not supposed to be green.");
    }

    @Override
    public Collection<String> getModuleExtensionInterfaces() {
        String[] interfaces = {"modules.organization.OrganizationPlugable"};
        return java.util.Arrays.asList(interfaces);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

}
