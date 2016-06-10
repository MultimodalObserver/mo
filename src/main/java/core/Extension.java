package core;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Celso
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Extension {
    String   codeName() default "";
    String   packageName() default "";
    String   fullName() default "";
    String   name() default "";
    String   description() default "";
    String[] interfacesThatImplements() default {};
    String[] interfacesThatProvide() default {};
}

//@Extension(
//        codeName = "",
//        packageName = "",
//        fullName = "",
//        name = "",
//        description = "",
//        interfacesThatImplements = {""},
//        interfacesThatProvide = {""}
//)