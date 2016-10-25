package mo.core.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

public class Utils {
    
    private static final Logger logger = Logger.getLogger(Utils.class.getName());

    public static void centerOnScreen(final Component c) {
        final int width = c.getWidth();
        final int height = c.getHeight();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width / 2) - (width / 2);
        int y = (screenSize.height / 2) - (height / 2);
        c.setLocation(x, y);
    }
    
    public static ImageIcon createImageIcon(String path, Class clazz) {
            java.net.URL imgURL = clazz.getClassLoader().getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            } else {
                logger.log(Level.WARNING, String.format("Couldn't find file: %s", path));
                return null;
            }
        }
}
