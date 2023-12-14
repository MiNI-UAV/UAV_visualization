package org.uav.audio;

import org.javatuples.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MusicPlayer {

    private File[] musicFiles;
    private AudioRenderer audioRenderer;
    private final float userVolume;
    private boolean isRunning = false;
    private int lastSong = -1;
    private final List<Consumer<Pair<MusicPlayerEvent,String>>> subscribers;
    private final Random random;

    public MusicPlayer(float userVolume) {
        this.userVolume = userVolume;
        musicFiles = new File[]{};
        subscribers = new ArrayList<>();
        random = new Random();
    }

    public int setDirectory(String path)
    {
        File musicFolder = new File(path);

        if (!musicFolder.isDirectory()) {
            System.err.println("Invalid music folder.");
            return -1;
        }

        musicFiles = musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".ogg"));

        if (musicFiles == null || musicFiles.length == 0) {
            System.err.println("No OGG files found in the folder.");
            return 0;
        }

        return musicFiles.length;

    }

    private boolean checkDirectory()
    {
        return musicFiles != null && musicFiles.length != 0;
    }

    private String play() {
        int nextSong = random.nextInt(musicFiles.length);
        if (nextSong == lastSong)
            nextSong = (nextSong + 1) % musicFiles.length;
        lastSong = nextSong;
        File oggFile = musicFiles[nextSong];
        var track = new VorbisTrack(oggFile.toString(), new AtomicInteger());
        audioRenderer = new AudioRenderer(track);
        audioRenderer.setGain(userVolume);
        audioRenderer.play();
        return oggFile.getName();
    }

    public void update() {
        if(!isRunning) return;
        boolean hasEnded = !audioRenderer.update(false);
        if(hasEnded)
            nextSong();
    }

    private void stop()
    {
        audioRenderer.close();
    }

    private void notifySubscriber(MusicPlayerEvent event, String content)
    {
        for (Consumer<Pair<MusicPlayerEvent,String>> subscriber : subscribers) {
            subscriber.accept(new Pair<>(event,content));
        }
    }

    public String playOrStop()
    {
        String returnValue = "";
        if(!checkDirectory()) return returnValue;

        if (isRunning) {
            stop();
            notifySubscriber(MusicPlayerEvent.STOP, "");
        } else {
            returnValue = play();
            notifySubscriber(MusicPlayerEvent.PLAY, returnValue);
        }
        isRunning = !isRunning;
        return returnValue;
    }

    public String nextSong()
    {
        String returnValue = "";
        if(!checkDirectory() || !isRunning) return returnValue;
        stop();
        returnValue = play();
        notifySubscriber(MusicPlayerEvent.NEXT, returnValue);
        return returnValue;
    }

    public  void subscribe(Consumer<Pair<MusicPlayerEvent,String>> subscriber) {
        subscribers.add(subscriber);
    }

    public void close() {
        audioRenderer.close();
    }
}
