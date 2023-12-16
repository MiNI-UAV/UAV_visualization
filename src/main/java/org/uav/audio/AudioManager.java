package org.uav.audio;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.model.SimulationState;
import org.uav.scene.camera.Camera;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioManager {

    private SoundListener listener;

    private final float soundVolumeMultiplier;
    List<AudioRenderer> propellerSounds;
    List<Vector3f> propellerOffsets;

    private final Camera camera;
    // https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter22/chapter22.html
    public AudioManager(SimulationState simulationState, DroneParameters droneParameters, Config config) {
        this.soundVolumeMultiplier = config.getAudioSettings().getSoundVolume();
        this.camera = simulationState.getCamera();
        listener = new SoundListener();
        propellerSounds = droneParameters.getRotors().getRotor().stream().map(r -> {
            var propellerTrack = new VorbisTrack(Paths.get(simulationState.getAssetsDirectory(), "audio", "drone_sound_mono.ogg").toString(), new AtomicInteger());
            var ar = new AudioRenderer((propellerTrack));
            ar.setGain(0);
            return ar;
        }).toList();
        propellerOffsets = droneParameters.getRotors().getRotor().stream().map(DroneParameters.Rotors.Rotor::getPosition).toList();
    }

    public void play() {
        propellerSounds.forEach(AudioRenderer::play);
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isPresent()) {
            var droneStatus = drone.get().droneStatus;
            var oms = droneStatus.propellersRadps;
            for(int i=0; i<propellerSounds.size() && i<oms.size(); i++) {
                var propellerSound = propellerSounds.get(i);
                var om = oms.get(i);
                var propellerOffset = new Vector3f(propellerOffsets.get(i)).mul(new Matrix3f().rotate(droneStatus.rotation));
                propellerSound.setGain(om*0.001f * soundVolumeMultiplier);
                propellerSound.setPitch(0.5f + om*0.001f);
                propellerSound.setPosition(new Vector3f(droneStatus.position).add(propellerOffset));
                propellerSound.setVelocity(droneStatus.linearVelocity);
            }
            // Od 0 do 1000

        } else {
            propellerSounds.forEach(p -> p.setGain(0));
        }
        listener.setOrientation(camera.getCameraFront(), camera.getCameraUp());
        listener.setPosition(camera.getCameraPos());
        listener.setVelocity(camera.getCameraVel());
        propellerSounds.forEach(ar -> ar.update(true));
    }
}