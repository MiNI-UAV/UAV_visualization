package org.uav.model;

import lombok.Data;
import org.uav.config.Config;
import org.uav.input.CameraMode;
import org.uav.model.status.DroneStatuses;
import org.uav.model.status.ProjectileStatuses;
import org.uav.scene.camera.Camera;

import java.util.concurrent.locks.ReentrantLock;

@Data
public class SimulationState {
    final long window;
    final DroneStatuses droneStatuses;
    final ReentrantLock droneStatusesMutex;
    final ProjectileStatuses projectileStatuses;
    final ReentrantLock projectileStatusesMutex;

    final Camera camera;
    CameraMode currentCameraMode;
    Drone currentlyControlledDrone;
    final DroneStatuses currPassDroneStatuses;
    final ProjectileStatuses currPassProjectileStatuses;

    float lastHeartBeatTimeStamp;


    public SimulationState(Config config, long window) {
        this.window = window;
        droneStatuses = new DroneStatuses();
        droneStatusesMutex = new ReentrantLock();
        projectileStatuses = new ProjectileStatuses();
        projectileStatusesMutex = new ReentrantLock();
        currentCameraMode = config.defaultCamera;
        currentlyControlledDrone = null;
        currPassDroneStatuses = new DroneStatuses(droneStatuses.map);
        currPassProjectileStatuses = new ProjectileStatuses(projectileStatuses.map);
        camera = new Camera(this, config);
    }
}
