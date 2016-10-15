package mo.visualization;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mo.core.ui.GridBConstraints;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
import org.apache.commons.lang3.time.FastDateFormat;

public class VisualizationPlayer {

    JPanel panel;
    JSlider slider;
    JButton playButton;

    Timer timer;

    long start, end, current;
    int interval = 100;
    boolean isPlaying = false;

    GridBConstraints gbc;

    int sliderPrecision = 1000; //seconds

    List<VisualizableConfiguration> configs;

    private JLabel ellapsedTLabel;
    private final static String ellapsedFormat = "%02d:%02d:%02d:%1d";
    private JLabel currentTime;

    private boolean sliderMovedProgrammatically;

    public VisualizationPlayer(List<VisualizableConfiguration> configurations) {
        configs = configurations;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                gbc = new GridBConstraints();
                panel = new JPanel(new GridBagLayout());

                long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
                for (VisualizableConfiguration config : configs) {
                    if (config.getStart() < min) {
                        min = config.getStart();
                    }
                    if (config.getEnd() > max) {
                        max = config.getEnd();
                    }
                }
                if (min == Long.MAX_VALUE) {
                    min = 0;
                }
                if (max == Long.MIN_VALUE) {
                    max = 100000;
                }

                current = start = min;

                end = max;
                
                System.out.println("max "+max + " min "+min);

                slider = new JSlider(0, (int) (max - min), 0);
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
                gbc.i(new Insets(5, 5, 5, 5));
                gbc.wx(1);
                panel.add(slider, gbc.gw(3));

                playButton = new JButton(">");
                playButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        playPressed();
                    }
                });
                panel.add(playButton, gbc.gy(1).gw(1).wx(0));

                ellapsedTLabel = new JLabel("00:00:00:000");
                currentTime = new JLabel("2016-10-15 00:00:00:0");
                panel.add(ellapsedTLabel, gbc.gx(1));

                panel.add(currentTime, gbc.gx(2));

                timer = new Timer(interval, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        timePassed(interval);
                    }
                });

                DockableElement d = new DockableElement();
                d.add(panel);
                DockablesRegistry.getInstance().addDockableInProjectGroup("", d);
                System.out.println("hola");
            }
        });

    }

    private void sliderMoved() {

        int val = slider.getValue();
        current = start + val;
        seek(current);
        updateUI();

    }

    private void seek(long millis) {
        for (VisualizableConfiguration config : configs) {
            config.seek(millis);
        }
    }

    private void playPressed() {
        if (current >= end) {
            if (isPlaying) {
                pause();
            }
        } else if (isPlaying) {
            pause();
        } else {
            play();
        }
    }

    private void pause() {
        pauseAll();
        isPlaying = false;
        timer.stop();
        playButton.setText(">");
    }

    private void play() {
        playAll();
        isPlaying = true;
        timer.start();
        playButton.setText("||");
    }

    private void timePassed(int interval) {

        current += interval;

        updateUI();
        if (current >= end) {
            pause();
        }
    }

    private void seekAll(long millis) {
        for (VisualizableConfiguration config : configs) {
            config.seek(current);
        }
    }

    private void playAll() {
        for (VisualizableConfiguration config : configs) {
            config.play();
        }
    }

    private void pauseAll() {
        for (VisualizableConfiguration config : configs) {
            config.pause();
        }
    }

    private void updateUI() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int ellapsed = (int) (current - start);
                long millis = (ellapsed % 1000) / 100;
                long second = (ellapsed / 1000) % 60;
                long minute = (ellapsed / (1000 * 60)) % 60;
                long hour = (ellapsed / (1000 * 60 * 60)) % 24;
                ellapsedTLabel.setText(String.format(ellapsedFormat, hour, minute, second, millis));

                Date d = new Date(current);
                FastDateFormat timeF = FastDateFormat.getInstance("yyyy-MM-dd  HH:mm:ss:SSS");
                currentTime.setText(timeF.format(d));
                
                sliderMovedProgrammatically = true;
                slider.setValue((int) (current - start));
                sliderMovedProgrammatically = false;
            }
        });

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);

        ArrayList<VisualizableConfiguration> configs = new ArrayList<>();
        VisualizationPlayer p = new VisualizationPlayer(configs);
        p.panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        //GridBConstraints gb = new GridBConstraints();
        //frame.add(p.panel, gb.f(GridBConstraints.BOTH));
        frame.setLayout(new BorderLayout());
        frame.add(p.panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

}
