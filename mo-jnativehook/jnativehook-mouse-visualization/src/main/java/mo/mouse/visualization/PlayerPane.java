package mo.mouse.visualization;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class PlayerPane extends JPanel {

    private double scale;
    private final List<Rectangle> screenBounds;

    private Point virtualPoint;
    private Point screenPoint;

    private Rectangle virtualBounds = new Rectangle(0, 0, 0, 0);

    public PlayerPane(List<Rectangle> screens) {
        screenBounds = screens;
        for (Rectangle screen : screens) {
            virtualBounds.add(screen);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        scale = getScaleFactorToFit(virtualBounds.getSize(), getSize());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int xOffset = 0;
        int yOffset = 0;
        List<Rectangle> scaledBounds = new ArrayList<>(screenBounds.size());
        for (Rectangle bounds : screenBounds) {
            bounds = scale(bounds);
            scaledBounds.add(bounds);
            if (bounds.x < xOffset) {
                xOffset = bounds.x;
            }
            if (bounds.y < yOffset) {
                yOffset = bounds.y;
            }
        }
        if (xOffset < 0) {
            xOffset *= -1;
        }
        if (yOffset < 0) {
            yOffset *= -1;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        for (Rectangle bounds : scaledBounds) {
            bounds.x += xOffset;
            bounds.y += yOffset;
            g2d.setColor(Color.DARK_GRAY);
            g2d.fill(bounds);
            g2d.setColor(Color.GRAY);
            g2d.draw(bounds);
        }

        FontMetrics fm = g2d.getFontMetrics();

        g2d.setColor(Color.WHITE);
        if (screenPoint != null) {
            int x = 0;
            int y = fm.getAscent();

            g2d.drawString(screenPoint.x + "," + screenPoint.y, x, y);
            screenPoint.x += xOffset;
            screenPoint.y += yOffset;
            screenPoint.x *= scale;
            screenPoint.y *= scale;
            g2d.fillOval(screenPoint.x - 2, screenPoint.y - 2, 4, 4);
        }

        if (virtualPoint != null) {
            int x = 0;
            int y = fm.getAscent() + fm.getHeight();

            g2d.drawString(virtualPoint.toString(), x, y);
        }

        g2d.dispose();
    }

    protected Rectangle scale(Rectangle bounds) {
        Rectangle scaled = new Rectangle(bounds);
        scaled.x *= scale;
        scaled.y *= scale;
        scaled.width *= scale;
        scaled.height *= scale;
        return scaled;
    }

    public void display(MouseEvent event) {
        screenPoint = new Point(event.x, event.y);
        repaint();
    }

    public static double getScaleFactorToFit(Dimension original, Dimension toFit) {
        double dScale = 1d;
        if (original != null && toFit != null) {
            double dScaleWidth = getScaleFactor(original.width, toFit.width);
            double dScaleHeight = getScaleFactor(original.height, toFit.height);
            dScale = Math.min(dScaleHeight, dScaleWidth);
        }
        return dScale;
    }

    public static double getScaleFactor(int iMasterSize, int iTargetSize) {
        double dScale = 1;
        dScale = (double) iTargetSize / (double) iMasterSize;
        return dScale;
    }
}
