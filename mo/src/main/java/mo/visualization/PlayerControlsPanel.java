package mo.visualization;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mo.core.ui.GridBConstraints;
import org.apache.commons.lang3.time.FastDateFormat;

public class PlayerControlsPanel {

    private JPanel panel;
    private JSlider slider;
    private JButton play;
    private int sliderPrecision = 1000; //seconds
    private GridBConstraints gbc;
    private boolean sliderMovedProgrammatically;
    private JLabel ellapsedTLabel;
    private final static String ellapsedFormat = "%02d:%02d:%02d:%1d";
    private JLabel currentTime;
    
    private VisualizationPlayer player;
    
    private long start, end;
    
    public PlayerControlsPanel(VisualizationPlayer player) {
        
        this.player = player;
        start = player.getStart();
        end = player.getEnd();
        
        panel = new JPanel(new GridBagLayout());
        
        SwingUtilities.invokeLater(() -> {
            gbc = new GridBConstraints();
            

            slider = new JSlider(0, (int) (player.getEnd() - player.getStart()), 0);
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    if (!slider.getValueIsAdjusting()) {
                        if (sliderMovedByUser()) {
                            sliderMoved();
                        }
                    }
                }

                private boolean sliderMovedByUser() {
                    return !sliderMovedProgrammatically;
                }
            });

            System.out.println(slider.getMinimum() + " " + slider.getMaximum());
            gbc.f(GridBagConstraints.HORIZONTAL);
            gbc.i(new Insets(5, 5, 5, 5)).wx(1);
            panel.add(slider, gbc.gw(3));

            play = new JButton(">");
            play.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    playPressed();
                }
            });
            panel.add(play, gbc.gy(1).gw(1).wx(0));

            ellapsedTLabel = new JLabel("00:00:00:000");
            currentTime = new JLabel("2016-10-15 00:00:00:0");
            panel.add(ellapsedTLabel, gbc.gx(1));

            panel.add(currentTime, gbc.gx(2));

        });
    }

    private void sliderMoved() {
        int val = slider.getValue();
        long current = player.getStart() + val;
        player.seek(current);
    }
    
    private void playPressed() {
        player.play();
    }

    public void setTime(long time) {
        SwingUtilities.invokeLater(() -> {
            int ellapsed = (int) (time - start);
            long millis = (ellapsed % 1000) / 100;
            long second = (ellapsed / 1000) % 60;
            long minute = (ellapsed / (1000 * 60)) % 60;
            long hour = (ellapsed / (1000 * 60 * 60)) % 24;
            ellapsedTLabel.setText(String.format(ellapsedFormat, hour, minute, second, millis));
            
            Date d = new Date(time);
            FastDateFormat timeF = FastDateFormat.getInstance("yyyy-MM-dd  HH:mm:ss:SSS");
            currentTime.setText(timeF.format(d));

            sliderMovedProgrammatically = true;
            slider.setValue((int) (time - start));
            sliderMovedProgrammatically = false;
        });
    }
    
    public JPanel getPanel() {
        return panel;
    }
}
