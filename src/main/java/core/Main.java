package core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class Main {
    ModuleDiscover md;
    DynamicClassLoader dcl;
    
    public Main(){
        md = new ModuleDiscover();
        dcl = new DynamicClassLoader();
        //md.findClassesThatImplements(Modulable.class, paths);
    }
    
    public Collection<String> getModulesClassesList(){
        return md.getModulesFullNameClasses();
    }
    
    public void InstanciateModule(String name) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        //try {
            //ClassReader cr = new ClassReader(name.replace(".", "/"));
            //Class<?> moduleClass = dcl.defineClass(name, cr.b);
            //ConstructorAccess access = ConstructorAccess.get(cr.getClassName());
            
//      SomeClass someObject = ...
//      MethodAccess access = MethodAccess.get(SomeClass.class);
//      access.invoke(someObject, "setName", "Awesome McLovin");
//      String name = (String)access.invoke(someObject, "getName");
            
            //Object someObject = (Modulable) access.newInstance();
            //MethodAccess maccess = MethodAccess.get(moduleClass.getClass());
            //maccess.invoke(someObject, "init");
            
            //URLClassLoader ucl = new URLClassLoader(new URL[]{fileEntry.toURI().toURL(), null});
            //ClassLoader cl = new ClassLoader
            Class<?> act = Class.forName(name); //only if its loaded
            //System.out.println(fileEntry.toURI().toURL());
                        //Class<?> cl = ucl.loadClass("myplugin."+baseName);
            
            //System.out.println(act.getCanonicalName());
            //Object asd = (act.getClass()) new Object();
            
            Method main = act.getMethod("init");
            main.setAccessible(true);
            Modulable t = (Modulable) act.newInstance();
            t.init();
            //main.invoke(t);
            
            //System.out.println("yap");
//        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
