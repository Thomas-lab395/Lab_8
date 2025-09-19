package spotify;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import javazoom.jl.decoder.JavaLayerException;

public class PlaybackEngine {

    private enum PlayerState {
        IDLE,
        PLAYING,
        PAUSED,
        FINISHED
    }

    private AdvancedPlayer audioPlayer;
    private Thread playbackThread;
    private String currentTrackPath;
    
    private volatile PlayerState currentState = PlayerState.IDLE;
    private int pausedOnFrame = 0;

    public synchronized void load(String filePath) {
        stop();
        currentTrackPath = filePath;
        currentState = PlayerState.IDLE;
    }

    public synchronized void play() {
        if (currentTrackPath == null || currentState == PlayerState.PLAYING) {
            return;
        }

        if (currentState == PlayerState.FINISHED) {
            pausedOnFrame = 0;
        }

        currentState = PlayerState.PLAYING;
        
        playbackThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(currentTrackPath)) {
                audioPlayer = new AdvancedPlayer(fis);
                audioPlayer.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent evt) {
                        pausedOnFrame = evt.getFrame();
                    }
                });
                
                audioPlayer.play(pausedOnFrame, Integer.MAX_VALUE);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JavaLayerException e) {
                
            } catch (IOException ex) {
                System.getLogger(PlaybackEngine.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            } finally {
                
                if (currentState != PlayerState.PAUSED) {
                    currentState = PlayerState.IDLE;
                    pausedOnFrame = 0;
                }
            }
        });
        
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public synchronized void pause() {
        if (currentState != PlayerState.PLAYING) {
            return;
        }
 
        currentState = PlayerState.PAUSED;
       
        if (audioPlayer != null) {
            audioPlayer.close();
        }
    }

    public synchronized void stop() {
        if (currentState == PlayerState.IDLE) {
            return;
        }
   
        currentState = PlayerState.IDLE;
       
        if (audioPlayer != null) {
            audioPlayer.close();
        }
     
    }

    public boolean isPlaying() {
        return currentState == PlayerState.PLAYING;
    }

    public boolean isPaused() {
        return currentState == PlayerState.PAUSED;
    }
}