package core;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import static org.objectweb.asm.Opcodes.ASM5;

public class ModuleClassVisitor extends ClassVisitor {
    List<String> modulesClasses;

    public ModuleClassVisitor() {
        super(ASM5);
        modulesClasses = new ArrayList<>();
    }

    @Override
    public void visit(int version, int access, String name,
            String signature, String superName, String[] interfaces) {

        for (String i : interfaces) {
            String modulable = Modulable.class.getCanonicalName();
            modulable = modulable.replace(".", "/");
            if (i.compareTo(modulable)==0){
                modulesClasses.add(name);
            }
        }
    }
    
    public List<String> getModules(){
        return modulesClasses;
    }
}
