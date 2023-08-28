package org.uav.audio;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import org.javatuples.Pair;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class MusicPlayer {

    private static File[] musicFiles;
    private static AdvancedPlayer player;
    private static boolean isRunning = false;
    private static int lastSong = -1;
    private static Semaphore semaphore = new Semaphore(1, true);
    private static List<Consumer<Pair<MusicPlayerEvent,String>>> subscribers = new ArrayList<>();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Random random = new Random();
    private static final PlaybackListener listener =  new PlaybackListener() {
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
    public static int setDirectory(String path)
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

        System.out.println(String.format("Found %d mp3 files", musicFiles.length));
        return musicFiles.length;

    }

    private static boolean checkDirectory()
    {
        if(musicFiles == null || musicFiles.length == 0)
        {
            System.out.println("No mp3 files found or directory not initialized");
            return false;
        }
        return true;
    }

    private static String play() throws JavaLayerException, FileNotFoundException
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

    private static void stop()
    {
        player.close();
    }

    private static void notifySubscriber(MusicPlayerEvent event, String content)
    {
        for (Consumer<Pair<MusicPlayerEvent,String>> subscriber : subscribers) {
            subscriber.accept(new Pair(event,content));
        }
    }

    public static String playOrStop()
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

    public static String nextSong()
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

    public static void subscribe(Consumer<Pair<MusicPlayerEvent,String>> subscriber) {
        subscribers.add(subscriber);
    }


        /// EXAMPLE
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MusicPlayer <music_folder>");
            System.exit(1);
        }

        String musicFolder = args[0];

        /// #1
        setDirectory(musicFolder);
        subscribe((Pair<MusicPlayerEvent, String> p) -> {
            System.out.println(String.format("Event: %s, message: %s",p.getValue0().toString(), p.getValue1()));
        });


        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Play/Pause");
            System.out.println("2. Next");
            System.out.println("3. Quit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    /// #2
                    playOrStop();
                    break;
                case 2:
                    /// #3
                    nextSong();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    return;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }
}
