package mo.visualization;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.core.ui.dockables.DockableElement;

public class VisualizationPlayer {

    private long start;
    private long end;
    private long current;
    private boolean isPlaying = false;

    private final List<VisualizableConfiguration> configs;
    
    private final PlayerControlsPanel panel;
    
    private Thread playerThread; 
    private static final Logger logger = Logger.getLogger(VisualizationPlayer.class.getName());
    
    private final DockableElement dockable;
    private boolean stopped;

    public VisualizationPlayer(List<VisualizableConfiguration> configurations) {
        configs = configurations;
        obtainMinAndMaxTime();

        panel = new PlayerControlsPanel(this);
        
        dockable = new DockableElement();
        dockable.add(panel.getPanel());
        //DockablesRegistry.getInstance().addDockableInProjectGroup("", d);
    }
    
    public DockableElement getDockable() {
        return dockable;
    }
    
    private void obtainMinAndMaxTime() {
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        for (VisualizableConfiguration config : configs) {
            if (config.getPlayer().getStart() < min) {
                min = config.getPlayer().getStart();
            }
            if (config.getPlayer().getEnd() > max) {
                max = config.getPlayer().getEnd();
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
    }

    public void seek(long millis) {
        
        if (millis < start) {
            millis = start;
        } else if (millis > end) {
            millis = end;
        }
        
        current = millis;
        
        for (VisualizableConfiguration config : configs) {
            config.getPlayer().seek(millis);
        }
    }

    public void pause() {
        //pauseAll();
        isPlaying = false;
        playerThread.interrupt();
        panel.stop();
        //playButton.setText(">");
    }
    
    public long getCurrentTime() {
        return current;
    }

    public void play() {
        playerThread = new Thread(() -> {
            isPlaying = true;
            while(!Thread.interrupted()) {
                
                if (current > end) {
                    if (stopped) {
                        current = start;
                        stopped = false;
                        isPlaying = true;
                    } else {
                        isPlaying = false;
                        for (VisualizableConfiguration config : configs) {
                            config.getPlayer().stop();
                        }
                        panel.stop();
                        current--;
                        stopped = true;
                        return;
                    }
                }
                
                long loopStart = System.nanoTime();
                
                for (VisualizableConfiguration config : configs) {
                    config.getPlayer().play(current);
                }
                
                panel.setTime(current);

                long loopEnd = System.nanoTime();
                long loopTime = loopEnd - loopStart;
                long timeToWait = 1000000 - loopTime;
                if (timeToWait > 0) {
                    try {
                        Thread.sleep(0, (int) timeToWait);
                    } catch (InterruptedException ex) {
                        //logger.log(Level.SEVERE, null, ex);
                        return;
                    }
                }
                current++;
            }
        });
        playerThread.start();
    }

    private void seekAll(long millis) {
        for (VisualizableConfiguration config : configs) {
            config.getPlayer().seek(current);
        }
    }

    private void playAll() {
        for (VisualizableConfiguration config : configs) {
            config.getPlayer().play(0); //TODO0000000000000
        }
    }

    private void pauseAll() {
        for (VisualizableConfiguration config : configs) {
            config.getPlayer().pause();
        }
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
