package org.uav.audio;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import org.javatuples.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class MusicPlayer {

    private File[] musicFiles;
    private AdvancedPlayer player;
    private boolean isRunning = false;
    private int lastSong = -1;
    private final Semaphore semaphore;
    private final List<Consumer<Pair<MusicPlayerEvent,String>>> subscribers;
    private final ExecutorService executorService;
    private final Random random;
    private final PlaybackListener listener;

    public MusicPlayer() {
        musicFiles = new File[]{};
        semaphore = new Semaphore(1, true);
        subscribers = new ArrayList<>();
        executorService = Executors.newSingleThreadExecutor();
        random = new Random();
        listener =  new PlaybackListener() {
            @Override
            public void playbackFinished(PlaybackEvent evt) {
                try {
                    semaphore.acquire(1);
                    player.close();
                    var songName = play();
                    notifySubscriber(MusicPlayerEvent.NEXT, songName);
                } catch (Exception e) {
                    notifySubscriber(MusicPlayerEvent.ERROR, e.getMessage());
                    e.printStackTrace();
                } finally {
                    semaphore.release(1);
                }
            }
        };
    }

    public int setDirectory(String path)
    {
        File musicFolder = new File(path);

        if (!musicFolder.isDirectory()) {
            System.out.println("Invalid music folder.");
            return -1;
        }

        musicFiles = musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

        if (musicFiles == null || musicFiles.length == 0) {
            System.out.println("No MP3 files found in the folder.");
            return 0;
        }

        return musicFiles.length;

    }

    private boolean checkDirectory()
    {
        return musicFiles != null && musicFiles.length != 0;
    }

    private String play() throws JavaLayerException, FileNotFoundException
    {
        int nextSong = random.nextInt(musicFiles.length);
        if (nextSong == lastSong)
        {
            nextSong = (nextSong + 1) % musicFiles.length;
        }
        lastSong = nextSong;
        File mp3File = musicFiles[nextSong];
        InputStream inputStream = new FileInputStream(mp3File);
        player = new AdvancedPlayer(inputStream);
        player.setPlayBackListener(listener);
        executorService.submit(() -> {
            try {
            player.play();
            }
            catch (Exception e)
            {
                notifySubscriber(MusicPlayerEvent.ERROR, e.getMessage());
                e.printStackTrace();
            }
        });
        return mp3File.getName();
    }

    private void stop()
    {
        player.close();
    }

    private void notifySubscriber(MusicPlayerEvent event, String content)
    {
        for (Consumer<Pair<MusicPlayerEvent,String>> subscriber : subscribers) {
            subscriber.accept(new Pair(event,content));
        }
    }

    public String playOrStop()
    {
        String returnValue = "";
        if(!checkDirectory()) return returnValue;
        try {
            semaphore.acquire(1);
            if (isRunning) {
                stop();
                notifySubscriber(MusicPlayerEvent.STOP, "");
            } else {
                returnValue = play();
                notifySubscriber(MusicPlayerEvent.PLAY, returnValue);
            }
            isRunning = !isRunning;
        }
        catch (Exception e)
        {
            notifySubscriber(MusicPlayerEvent.ERROR, e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            semaphore.release(1);
            return returnValue;
        }
    }

    public String nextSong()
    {
        String returnValue = "";
        if(!checkDirectory() || !isRunning) return returnValue;
        try {
            semaphore.acquire(1);
            stop();
            returnValue = play();
            notifySubscriber(MusicPlayerEvent.NEXT, returnValue);
        }
        catch (Exception e)
        {
            notifySubscriber(MusicPlayerEvent.ERROR, e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            semaphore.release(1);
        }
        return returnValue;
    }

    public  void subscribe(Consumer<Pair<MusicPlayerEvent,String>> subscriber) {
        subscribers.add(subscriber);
    }

    public void close() {
        if(player != null) player.close();
        executorService.shutdownNow();
    }
}
