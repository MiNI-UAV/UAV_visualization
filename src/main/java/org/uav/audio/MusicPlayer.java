package org.uav.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MusicPlayer {

    AudioInputStream audioInputStream;
    Clip clip;

    public MusicPlayer(String filePath) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
    }

    public void play() {
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        clip.stop();
    }
    public void setVolume(float level) {
        FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
        if (volume != null) {
            volume.setValue(level / 100.f);
        }

    }
}
