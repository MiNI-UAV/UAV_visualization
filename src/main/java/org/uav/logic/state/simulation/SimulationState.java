package org.uav.logic.state.simulation;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Vector3f;
import org.uav.logic.communication.DroneCommunication;
import org.uav.logic.config.Config;
import org.uav.logic.config.DroneParameters;
import org.uav.logic.fps.FpsCounter;
import org.uav.logic.input.handler.JoystickStatus;
import org.uav.logic.state.controlMode.ControlModeDemanded;
import org.uav.logic.state.drone.DroneStatuses;
import org.uav.logic.state.notifications.Notifications;
import org.uav.logic.state.projectile.Projectile;
import org.uav.logic.state.projectile.ProjectileStatuses;
import org.uav.presentation.entity.camera.Camera;
import org.uav.presentation.entity.camera.CameraMode;
import org.uav.presentation.entity.drone.DroneState;

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
    DroneCommunication currentlyControlledDrone;
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
        skyColor = new Vector3f(ArrayUtils.toPrimitive(config.getSceneSettings().getSkyColor(), 0.0F));
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
        if(getCurrentlyControlledDrone().isEmpty()) return Optional.empty();
        var drone = dronesInAir.get(getCurrentlyControlledDrone().get().getId());
        return Optional.ofNullable(drone);
    }

    public Optional<DroneCommunication> getCurrentlyControlledDrone() {
        return Optional.ofNullable(currentlyControlledDrone);
    }
}
