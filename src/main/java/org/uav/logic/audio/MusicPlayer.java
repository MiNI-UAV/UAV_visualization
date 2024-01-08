package org.uav.logic.audio;

import org.uav.logic.messages.Message;
import org.uav.logic.messages.Publisher;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MusicPlayer implements Publisher {

    public static final String MESSAGE_CATEGORY_MUSIC = "Music";
    private File[] musicFiles;
    private AudioRenderer audioRenderer;
    private final float userVolume;
    private boolean isRunning = false;
    private int lastSong = -1;
    private final List<Consumer<Message>> subscribers;
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
            notifySubscribers(new Message("Invalid music folder.", MESSAGE_CATEGORY_MUSIC, 100));
            return -1;
        }

        musicFiles = musicFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".ogg"));

        if (musicFiles == null || musicFiles.length == 0) {
            notifySubscribers(new Message("No OGG files found in the music folder.", MESSAGE_CATEGORY_MUSIC, 100));
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

    public String playOrStop()
    {
        String returnValue = "";
        if(!checkDirectory()) return returnValue;

        if (isRunning) {
            stop();
            notifySubscribers(new Message("Music turned OFF", MESSAGE_CATEGORY_MUSIC, 4));
        } else {
            returnValue = play();
            notifySubscribers(new Message("Music turned ON", MESSAGE_CATEGORY_MUSIC, 4));
            notifyOfPlaying(returnValue);
        }
        isRunning = !isRunning;
        return returnValue;
    }

    private void notifyOfPlaying(String returnValue) {
        notifySubscribers(new Message(MessageFormat.format("Playing now: {0}", returnValue), MESSAGE_CATEGORY_MUSIC, 6));
    }

    public String nextSong()
    {
        String returnValue = "";
        if(!checkDirectory() || !isRunning) return returnValue;
        stop();
        returnValue = play();
        notifyOfPlaying(returnValue);
        return returnValue;
    }

    public void close() {
        if(audioRenderer != null) audioRenderer.close();
    }

    @Override
    public List<Consumer<Message>> getSubscribers() {
        return subscribers;
    }
}
