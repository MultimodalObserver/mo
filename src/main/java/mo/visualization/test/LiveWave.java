package mo.visualization.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class LiveWave extends JPanel {
    BufferedImage image;
    Graphics2D graphics;
    
    int width = 500, height = 200;
    
    ArrayList<Variable> variables;
    
    int whiteSpaceWidth = 50;
    int pointWidth = 1;
    int pointHeight = 2;
    
    int pointDistance = 10;
    
    int prevX;
    int prevY;
    
    long lastTimestamp = 0;

    public LiveWave() {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics = image.createGraphics();
        graphics.setBackground(Color.white);
        graphics.fillRect(0, 0, width, height);
        variables = new ArrayList<>();
        setDoubleBuffered(true);
    }
    
    public static void main(String[] args) {
        LiveWave w = new LiveWave();
        w.addVariable("test", 0, 100, null);
        
        JFrame f = new JFrame("Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(300, 100));
        f.add(w);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                f.setVisible(true);
            }
        });
        
        int start = randInt(0,100);
        long sleep = 0;
        while(true) {
            
            //w.addData("test", next(start));
            w.addData("test", System.currentTimeMillis(), randInt(0,100));
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ex) {
                Logger.getLogger(LiveWave.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    static int next(int last) {
        Random r = new Random();
        int n = r.nextInt();
        if (n % 3 == 0) {
            last++;
        } else if (n % 2 == 0) {
            last--;
        }
        return last;
    }
    
    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    
    void addData(String variableName, long timestamp, double value) {
        for (Variable variable : variables) {
            if (variable.name.equals(variableName)) {
                draw(variable, timestamp, value);
            }
        }
    }
    
    void draw(Variable v, long timestamp, double value) {
        
        if (timestamp > lastTimestamp) {
            graphics.copyArea(0, 0, width, height, -pointDistance, 0);
            graphics.setColor(graphics.getBackground());
            graphics.fillRect(width-whiteSpaceWidth, 0, height-whiteSpaceWidth, height);
        }
        
        lastTimestamp = timestamp;
        
        int mappedValue =  (int) ((int) (value - v.min) / (v.max - v.min) * height) ;
        int inverted = height - mappedValue;
        
        graphics.setColor(v.color);
        
        

        
        
        int x = width-whiteSpaceWidth;
        int y = inverted;
        
        if (prevX == 0 && prevY == 0) {
            prevX = x;
            prevY = y;
        }
        
        graphics.drawLine(prevX, prevY, x, y);
        prevX = x - pointDistance;
        prevY = y;
        //graphics.drawRect(width-whiteSpaceWidth, inverted, pointWidth, pointHeight);

        FontMetrics m = graphics.getFontMetrics();
        String val = value + "";
        int valWidth = m.stringWidth(val);
        graphics.drawString(val, width - valWidth, height - 40);
        
        String time = timestamp + "";
        int timeWidth = m.stringWidth(time);
        if (timeWidth < whiteSpaceWidth) {
            graphics.drawString(time, width - timeWidth, height - 20);
        } 
        
        
        repaint();
    }

    void addVariable(String name, double min, double max, Color color) {
        for (Variable variable : variables) {
            if (variable.name.equals(name)) {
                System.out.println("Name <"+name+"> already defined");
                return;
            }
        }
        
        Variable v = new Variable();
        v.name = name;
        v.min = min;
        v.max = max;
        if (color != null) {
            v.color = color;
        }
        
        variables.add(v);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int width = this.getWidth();
        int height = this.getHeight();
        
        g.drawImage(image,
                1, 1,
                width - 0,
                height - 0,
                0, 0,
                image.getWidth(), image.getHeight(),
                null);
    }
    
    class Variable {
        Color color = Color.BLACK;
        String name;
        double min = 0, max = height;
    }
}
