package org.uav.model;

import lombok.Data;
import org.uav.config.Config;
import org.uav.input.CameraMode;
import org.uav.model.status.DroneStatuses;
import org.uav.model.status.ProjectileStatuses;

import java.util.concurrent.locks.ReentrantLock;

@Data
public class SimulationState {
    final long window;
    final DroneStatuses droneStatuses;
    final ReentrantLock droneStatusesMutex;
    final ProjectileStatuses projectileStatuses;
    final ReentrantLock projectileStatusesMutex;
    CameraMode currentCameraMode;
    Drone currentlyControlledDrone;

    public SimulationState(Config config, long window) {
        this.window = window;
        droneStatuses = new DroneStatuses();
        droneStatusesMutex = new ReentrantLock();
        projectileStatuses = new ProjectileStatuses();
        projectileStatusesMutex = new ReentrantLock();
        currentCameraMode = config.defaultCamera;
        currentlyControlledDrone = null;
    }
}
