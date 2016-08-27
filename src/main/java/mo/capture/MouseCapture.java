package mo.capture;

import javax.swing.JMenuItem;
import mo.organization.CaptureProvider;

public class MouseCapture implements CaptureProvider {

    @Override
    public JMenuItem getMenu() {
        return new JMenuItem("Mouse");
    }
    
}
