package mo.core.plugin;

import mo.core.Utils;
import mo.example.IExtPointExample;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author Celso
 */
public class ExtensionScanner extends ClassVisitor {

    private static final String EXTENSION_DESC = "Lcore/plugin/Extension;";
    
    private static final 
            String EXTENSION_POINT_DESC = "Lcore/plugin/Extends;";
    
    private Plugin plugin = new Plugin();
    
    private boolean isExtension = false;
    
    public ExtensionScanner(int i) {    
        super(i);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        //System.out.println(version+" "+access+" "+name+" "+signature+" "+superName+" "+interfaces);
        plugin.setId(name.replace("/", "."));
        try {
            plugin.setClazz(Class.forName(name.replace("/", ".")));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ExtensionScanner.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        //System.out.println("name "+name);
        //System.out.println(" visitAnnotation: desc=" + desc + " visible=" + visible);
        if (desc.compareTo(EXTENSION_DESC) == 0) {
            //plugin = new Plugin();
            //System.out.println(" paso");
            isExtension = true;
            return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {
                
                @Override
                public void visit(String nam, Object value) {
                    //System.out.println("  visit " + nam + " " + value);
                    switch (nam) {
                        case "id":
                            plugin.setId((String) value);
                            break;
                        case "version":
                            plugin.setVersion((String) value);
                            break;
                        case "name":
                            plugin.setName((String) value);
                            break;
                        case "description":
                            plugin.setDescription((String) value);
                            break;
                        default:
                            break;
                    }
                    super.visit(nam, value);
                }
                
                @Override
                public AnnotationVisitor visitArray(String string) {
                    
                    return new AnnotationVisitor(Opcodes.ASM5, super.visitArray(string)) {
                        
                        @Override
                        public AnnotationVisitor visitAnnotation(String name, String desc) {
                            Dependency d = new Dependency();
                            //System.out.println("   vann " + name + " " + desc);
                            if (desc.compareTo(EXTENSION_POINT_DESC) == 0) {
                                return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(name, desc)) {
                                    @Override
                                    public void visit(String name, Object value) {
                                        //System.out.println("v=" + name + " value=" + value);
                                        switch (name) {
                                            case "extensionPointId":
                                                d.setId((String) value);
                                                //System.out.println(name+"="+value);
                                                break;
                                            case "extensionPointVersion":
                                                d.setVersion((String) value);
                                                //System.out.println(name+"="+value);
                                                
                                                break;
                                            default:
                                                break;
                                        }
                                        //System.out.println("    visit " + name + " " + value);
                                        super.visit(name, value); //To change body of generated methods, choose Tools | Templates.
                                    }

                                    @Override
                                    public void visitEnd() {
                                        //System.out.println(" > "+d.getId() + " " + d.getVersion());
                                        plugin.addDependency(d);
                                        super.visitEnd(); //To change body of generated methods, choose Tools | Templates.
                                    }
                                };
                            } else {
                                return super.visitAnnotation(name, desc);
                            }
                            
                        }
                        
                    };
                }
            };
        } else {
            return super.visitAnnotation(desc, visible);
        }
    }

    @Override
    public void visitEnd() {
        if (!isExtension) this.plugin = null;
        super.visitEnd(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    public Plugin getPlugin(){
        return this.plugin;
    }

    public static void main(String[] args) throws Exception {
        String[] exts = {"class"};
        File path = new File(Utils.getBaseFolder());
        Collection<File> files = FileUtils.listFiles(path, exts, true);
        for (File file : files) {
            FileInputStream in = new FileInputStream(file);
            ExtensionScanner exScanner = new ExtensionScanner(Opcodes.ASM5);
            ClassReader cr = new ClassReader(in);
            cr.accept(exScanner, 0);
            
            if (exScanner.plugin!= null) {
                IExtPointExample i =(IExtPointExample) exScanner.getPlugin().getClazz().newInstance();
                i.SayHi();
            }
        }
    }
    
}
