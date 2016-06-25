package mo.pruebas;

import mo.core.plugin.Extension;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 *
 * @author Celso
 */
public class ReflectionsPrueba {

    public static void main(String[] args) {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                .setScanners(
                        new SubTypesScanner(false /* don't exclude Object.class */),
                        new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(
                        classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("core"))));
        //new FilterBuilder().
        reflections.getAllTypes().stream().forEach((allType) -> {
            System.out.println(allType);
        });
        
        reflections = new Reflections();
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Extension.class);
        for (Class<?> class1 : annotated) {
            System.out.println(class1);
        }
        
    }

}
