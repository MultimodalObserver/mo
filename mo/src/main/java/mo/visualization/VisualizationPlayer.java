package mo.visualization;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.border.BevelBorder;
import mo.core.ui.dockables.DockableElement;

public class VisualizationPlayer {

    private long start, end, current;
    private int interval = 100;
    private boolean isPlaying = false;

    private List<VisualizableConfiguration> configs;
    
    private PlayerControlsPanel panel;
    
    private Thread playerThread; 
    private Logger logger = Logger.getLogger(VisualizationPlayer.class.getName());
    
    private DockableElement dockable;

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
    }

    public void seek(long millis) {
        
        if (millis < start) {
            millis = start;
        } else if (millis > end) {
            millis = end;
        }
        
        current = millis;
        
        for (VisualizableConfiguration config : configs) {
            config.seek(millis);
        }
    }

    public void pause() {
        //pauseAll();
        isPlaying = false;
        playerThread.interrupt();
        //playButton.setText(">");
    }
    
    public long getCurrentTime() {
        return current;
    }

    public void play() {
        playerThread = new Thread(() -> {
            isPlaying = true;
            while(!Thread.interrupted()) {
                
                long loopStart = System.nanoTime();
                
                for (VisualizableConfiguration config : configs) {
                    config.play(current);
                }
                
                panel.setTime(current);

                long loopEnd = System.nanoTime();
                
                long loopTime = loopEnd - loopStart;
                
                long timeToWait = 1000000 - loopTime;
                
                if (timeToWait > 0) {
                    try {
                        Thread.sleep(0, (int) timeToWait);
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
                
                if (current >= end) {
                    break;
                }
                current++;
            }
        });
        playerThread.start();
        
        
        
//        if (current >= end) {
//            if (isPlaying) {
//                pause();
//            }
//        } else if (isPlaying) {
//            pause();
//            return;
//        } else {
//            //play();
//        }
//
//        long lastLoopTime = System.nanoTime();
//        final int TARGET_FPS = 60;
//        final long OPTIMAL_TIME = 1_000_000_000 / TARGET_FPS;
//
//        long fps = 0;
//        long lastFpsTime = 0;
//
//        // keep looping round til the game ends
//        while (isPlaying) {
//            // work out how long its been since the last update, this
//            // will be used to calculate how far the entities should
//            // move this loop
//            long now = System.nanoTime();
//            long updateLength = now - lastLoopTime;
//            lastLoopTime = now;
//            double delta = updateLength / ((double) OPTIMAL_TIME);
//
//            // update the frame counter
//            lastFpsTime += updateLength;
//            fps++;
//
//            // update our FPS counter if a second has passed since
//            // we last recorded
//            if (lastFpsTime >= 1000000000) {
//                System.out.println("(FPS: " + fps + ")");
//                lastFpsTime = 0;
//                fps = 0;
//            }
//
//            // update the game logic
//            doGameUpdates(delta);
//
//            // draw everyting
//            render();
//
//            // we want each frame to take 10 milliseconds, to do this
//            // we've recorded when we started the frame. We add 10 milliseconds
//            // to this and then factor in the current time to give 
//            // us our final value to wait for
//            // remember this is in ms, whereas our lastLoopTime etc. vars are in ns.
////            try {
////                Thread.sleep((lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000);
////            } catch (Exception ex) {}
//        }
    }

    private void seekAll(long millis) {
        for (VisualizableConfiguration config : configs) {
            config.seek(current);
        }
    }

    private void playAll() {
        for (VisualizableConfiguration config : configs) {
            config.play(0); //TODO0000000000000
        }
    }

    private void pauseAll() {
        for (VisualizableConfiguration config : configs) {
            config.pause();
        }
    }

    private void doGameUpdates(double delta) {
        panel.setTime(current);
    }

    private void render() {
        throw new UnsupportedOperationException("Not supported yet.");
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
