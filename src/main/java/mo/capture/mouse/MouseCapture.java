package mo.capture.mouse;

import javax.swing.JMenuItem;
import mo.capture.CaptureProvider;
import mo.core.plugin.Extends;
import mo.core.plugin.Extension;

@Extension(
        xtends = {
            @Extends(extensionPointId = "mo.capture.CaptureProvider-")
        }
)
public class MouseCapture implements CaptureProvider {

    @Override
    public JMenuItem getMenu() {
        return new JMenuItem("Mouse");
    }
    
}
