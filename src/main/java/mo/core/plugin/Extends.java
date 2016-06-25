package mo.core.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Celso
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Extends {
    String extensionPointId();
    String extensionPointVersion() default ">=0";
}
