package mo.visualization.mouse;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class LiveWave extends JPanel {
    BufferedImage image;
    Graphics2D graphics;

    public LiveWave() {
        image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
    }
    
    
}
