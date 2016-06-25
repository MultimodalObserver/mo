package mo.example;

import mo.core.plugin.Extends;
import mo.core.plugin.Extension;

/**
 *
 * @author Celso
 */
@Extension(
        id = "example.ExtensionExample",
        name = "Example",
        description = "Description",
        version = "0",
        xtends = {
            @Extends(
                    extensionPointId = "example.IExtPointExample",
                    extensionPointVersion = ">=0"
            ),
            @Extends(
                    extensionPointId = "example.IExtPointExample2",
                    extensionPointVersion = ">=0"
            )
        }
)
public class ExtensionExample implements mo.example.IExtPointExample {

    @Override
    public void SayHi() {
        System.out.println("Hi ^^");
    }

}
