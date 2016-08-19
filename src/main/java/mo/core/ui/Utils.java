package mo.core.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

public class Utils {

    public static void centerOnScreen(final Component c) {
        final int width = c.getWidth();
        final int height = c.getHeight();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width / 2) - (width / 2);
        int y = (screenSize.height / 2) - (height / 2);
        c.setLocation(x, y);
    }
}
