package mo.core.ui.dockables;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.util.xml.XElement;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Celso Gutiérrez <celso.gutierrez@usach.cl>
 */
public class FrameRegistry1 {
    CControl control;
    //private List<Dockable>
    public static void main(String[] args) {
        JFrame frame = new  JFrame("Title");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setLayout(new BorderLayout());
        
        CControl control = new CControl(frame);
        frame.add(control.getContentArea());

        Factory factory = new Factory();
        //Layout layout = new Layout();

        control.addMultipleDockableFactory("factory", factory);
        
        Dockable d = new Dockable(factory, new Layout("holanda"));
        d.add(new JLabel("holanda"));
        control.addDockable(d);
        d.setVisible(true);
        
        Factory factory2 = new Factory();
        control.addMultipleDockableFactory("factory2", factory2);
        Dockable d2 = new Dockable(factory2, new Layout("hola"));
        d2.add(new JLabel("holi"));
        
        control.addDockable(d2);
        d2.setVisible(true);
        
        Dockable d3 = new Dockable(factory, new Layout("Chau"));
        control.addDockable(d3);
        d3.setVisible(true);
        
        
        //control.
        XElement root = new XElement("root");
        Layout la = new Layout();
        control.writeXML(root);
        
        System.out.println(root.toString());
        
        frame.setPreferredSize(new Dimension(200, 200));
        frame.pack();
        frame.setVisible(true);
    }
    
    public void addPanel(JPanel panel) {
        
    }
    
    private static class Dockable extends DefaultMultipleCDockable {

        public Dockable(Factory factory, Layout layout) {
            super(factory);
            
            setTitleText( layout.getTitle() );
            add(new JLabel("asd"));
        }
        
        public Layout getLayout() {
            return new Layout(this.getTitleText());
        }
    }
    
    private static class Layout implements MultipleCDockableLayout{
        String title;
        
        public Layout() {
        
        }
        
        public Layout(String title) {
            this.title = title;
        }
        
        public String getTitle() {
            return this.title;
        }
        
        @Override
        public void writeStream(DataOutputStream stream) throws IOException {
            System.out.println("writeStream");
        }

        @Override
        public void readStream(DataInputStream stream) throws IOException {
            System.out.println("readStream");
        }

        @Override
        public void writeXML(XElement xe) {
            xe.addElement("title").setString(title);
            //System.out.println(xe);
            //System.out.println("writexml");
        }

        @Override
        public void readXML(XElement xe) {
            System.out.println("readxml");
        }

    }
    
    private static class Factory implements 
            MultipleCDockableFactory <Dockable,Layout>{

        @Override
        public Layout create() {
            return new Layout();
        }

        @Override
        public Dockable read(Layout l) {
            return new Dockable(this, l);
        }


        @Override
        public Layout write(Dockable f) {
//            System.out.println("write");
//            System.out.println(f);
//            return new Layout();
              return f.getLayout();
        }

        @Override
        public boolean match(Dockable f, Layout l) {
            return false;
        }
    }
}
