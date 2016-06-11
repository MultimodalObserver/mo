package core;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 *
 * @author Celso
 */
public class WizardDialog extends JDialog {
    private ArrayList<JPanel> panels;
    private JPanel cards, stepsPanel;
    private ArrayList<JLabel> stepsLabels;
    private HashMap<String,Object> result;
    private JButton backButton;
    private JButton nextButton;
    private JButton finishButton;
    private JLabel warningLabel;
    private JLabel stepTitleLabel;
    
    public WizardDialog(JFrame parent, String title){
        super(parent, title);
        super.setModalityType(ModalityType.APPLICATION_MODAL);
        this.panels = new ArrayList<>();
        result = new HashMap<>();
        stepsLabels = new ArrayList<>();
        
        //setLayout(new BoxLayout(dialogPanel, BoxLayout.LINE_AXIS));
        
        JLabel stepsLabel = new JLabel("Steps", JLabel.LEFT);
        stepsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JSeparator sep = new JSeparator();
        sep.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        stepsPanel = new JPanel();
        stepsPanel.setBackground(Color.WHITE);
        stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
        stepsPanel.add(stepsLabel);
        stepsPanel.add(sep);
        
        JPanel externalStepsPanel = new JPanel();
        externalStepsPanel.setBackground(Color.WHITE);
        externalStepsPanel.add(stepsPanel);
        
        backButton = new JButton("< Back");
        backButton.setEnabled(false);
        
        nextButton = new JButton("Next >");
        nextButton.setEnabled(false);
        
        finishButton = new JButton("Finish");
        finishButton.setEnabled(false);
        finishButton.addActionListener((ActionEvent e) -> {
            setVisible(false);
            dispose();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener((ActionEvent e) -> {
            result = null;
            setVisible(false);
            dispose();
        });
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        //buttonsPanel.setBorder(new EmptyBorder(20, 10, 10, 10));
        buttonsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK),
                BorderFactory.createEmptyBorder(20, 10, 10, 10)));
        buttonsPanel.add(Box.createGlue());
        buttonsPanel.add(backButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(nextButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(finishButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(cancelButton);
        
        
        stepTitleLabel = new JLabel("A Title");
        stepTitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JSeparator stepTitleSep = new JSeparator();
        stepTitleSep.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel panelForTitle = new JPanel();
        panelForTitle.setLayout(new BoxLayout(panelForTitle, BoxLayout.Y_AXIS));
        panelForTitle.add(stepTitleLabel);
        panelForTitle.add(stepTitleSep);
        
        cards = new JPanel(new CardLayout());
        
        warningLabel = new JLabel("A warning");
        warningLabel.setForeground(Color.red);
        warningLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        mainPanel.add(panelForTitle, BorderLayout.NORTH);

        mainPanel.add(cards, BorderLayout.CENTER);
        mainPanel.add(warningLabel, BorderLayout.SOUTH);
        
        //JPanel stepsPanel = new JPanel();
        Container contentPane = super.getContentPane();
        contentPane.add(externalStepsPanel, BorderLayout.WEST);
        contentPane.add(mainPanel, BorderLayout.CENTER);
        contentPane.add(buttonsPanel, BorderLayout.PAGE_END);
        //contentPane.add(buttons, BorderLayout.PAGE_END);
        
        super.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                result = null;
            }
            @Override
            public void windowClosing(WindowEvent e) {
                result = null;
            }
        });
    }
    
    public WizardDialog(JFrame parent, String title, Collection<JPanel> panels){
        super(parent, title);
        this.panels = new ArrayList<>();
    }
    
    public void addPanel(int index, JPanel panel){
        
    }
    
    public void addPanel(JPanel panel){
    
        if (panels == null) {
            panels = new ArrayList<>();
        }
        
        panels.add(panel);
    }
    
    public void addPanels(Collection<JPanel> panelsToAdd){
        
        if (panels == null){
            panels = new ArrayList<>();
        }
        
        panels.addAll(panelsToAdd);
    }
    
    public HashMap showWizard(){
        JLabel l = new JLabel();
        Font f = l.getFont();
        f = new Font(f.getFontName(), Font.PLAIN, f.getSize());
        for (int i=0; i<panels.size(); i++) {
            JPanel p = panels.get(i);
            cards.add(p.getName(), p);
            
            
            JLabel label = new JLabel((i+1)+".  "+p.getName());
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            label.setFont(f);
            stepsLabels.add(label);
            stepsPanel.add(stepsLabels.get(stepsLabels.size()-1));
        }
        stepsPanel.add(Box.createVerticalGlue());
        setStepTitle("1.  "+panels.get(0).getName());
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        return result;
    }
    
    public void addResult(String k, Object v){
        if (result == null)
            result = new HashMap<>();
        
        if (result.containsKey(k)) {
            result.replace(k, v);
        } else {
            result.put(k, v);
        }
    }
    
    public void nullResult(){
        result = null;
    }
    
    public void enableNext(){
        nextButton.setEnabled(true);
    }
    
    public void enableBack(){
        backButton.setEnabled(true);
    }
    
    public void enableFinish(){
        finishButton.setEnabled(true);
    }
    
    public void disableNext(){
        nextButton.setEnabled(false);
    }
    
    public void disableBack(){
        backButton.setEnabled(false);
    }
    
    public void disableFinish(){
        finishButton.setEnabled(false);
    }
    
    public void showPanel(String name){
        CardLayout cl = (CardLayout)(cards.getLayout());
        cl.show(cards, name);
    }
    
    public void setWarningMessage(String message){
        warningLabel.setText(message);
    }
    
    public void setStepTitle(String title){
        stepTitleLabel.setText(title);
    }
    
    public static void main(String[] args){
        JFrame f = new JFrame("asd aisdoia sd hasodhas hdoaih sd");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(100,100);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        
        JPanel p = new JPanel();
        p.setName("Terminar la tesis");
        p.add(new JLabel("asd"));
        
        JPanel pp = new JPanel();
        pp.setName("Encontrar trabajo");
        pp.add(new JLabel("hgdf"));
        
        WizardDialog w = new WizardDialog(f, "Hola");
        w.addPanel(p);
        w.addPanel(pp);
        //w.setSize(300,300);
        HashMap result = w.showWizard();
        if (null==result){
            System.out.println("  null");
        } else {
            System.out.println("  no null");
        }
        System.exit(0);
    }
}
