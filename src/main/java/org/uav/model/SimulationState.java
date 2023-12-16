package org.uav.model;

import lombok.Data;
import lombok.Getter;
import org.joml.Vector3f;
import org.uav.FpsCounter;
import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.input.CameraMode;
import org.uav.model.controlMode.ControlModeDemanded;
import org.uav.model.status.DroneState;
import org.uav.model.status.DroneStatuses;
import org.uav.model.status.JoystickStatus;
import org.uav.model.status.ProjectileStatuses;
import org.uav.scene.camera.Camera;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class SimulationState {
    String assetsDirectory;
    String serverMap;
    String droneModelChecksum;
    final long window;
    @Getter
    float lastSimulationTimeS;
    float simulationTimeS;
    FpsCounter fpsCounter;
    final Camera camera;

    JoystickStatus joystickStatus;
    final DroneStatuses droneStatuses;
    final ReentrantLock droneStatusesMutex;
    final ProjectileStatuses projectileStatuses;
    final ReentrantLock projectileStatusesMutex;
    final Notifications notifications;

    final Map<Integer, DroneState> dronesInAir;
    final ProjectileStatuses currPassProjectileStatuses;

    CameraMode currentCameraMode;
    Drone currentlyControlledDrone;
    ControlModeDemanded currentControlModeDemanded;
    float lastHeartBeatTimeStamp;
    boolean mapOverlay;
    float mapZoom;
    Vector3f skyColor;
    boolean spotLightOn;

    int currentlyChosenAmmo;
    final List<Projectile>ammos;
    int currentlyChosenCargo;
    final List<Projectile> cargos;


    public SimulationState(long window, Config config, DroneParameters droneParameters) {
        this.window = window;
        lastSimulationTimeS = 0;
        simulationTimeS = 0;
        droneStatuses = new DroneStatuses();
        droneStatusesMutex = new ReentrantLock();
        projectileStatuses = new ProjectileStatuses();
        projectileStatusesMutex = new ReentrantLock();
        notifications = new Notifications();
        currentCameraMode = config.getDroneSettings().getDefaultCamera();
        currentControlModeDemanded = null;
        currentlyControlledDrone = null;
        dronesInAir = new HashMap<>();
        currPassProjectileStatuses = new ProjectileStatuses(projectileStatuses.map);
        camera = new Camera(this, config);
        mapOverlay = false;
        mapZoom = 1;
        skyColor = new Vector3f(0.529f, 0.808f, 0.922f);
        currentlyChosenAmmo = 0;
        ammos = droneParameters.getAmmo() == null? new ArrayList<>():
                droneParameters.getAmmo().stream().map(e -> new Projectile(e, this)).toList();
        currentlyChosenCargo = 0;
        cargos = droneParameters.getCargo() == null? new ArrayList<>():
                 droneParameters.getCargo().stream().map(e -> new Projectile(e, this)).toList();
        fpsCounter = new FpsCounter();
        spotLightOn = false;
    }

    public void setSimulationTimeS(float simulationTimeS) {
        lastSimulationTimeS = simulationTimeS;
        this.simulationTimeS = simulationTimeS;
    }

    public Optional<DroneState> getPlayerDrone() {
        var drone = dronesInAir.get(getCurrentlyControlledDrone().getId());
        return Optional.ofNullable(drone);
    }
}
