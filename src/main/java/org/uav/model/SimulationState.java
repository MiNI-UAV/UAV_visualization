package org.uav.model;

import lombok.Data;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.uav.config.Config;
import org.uav.input.CameraMode;
import org.uav.model.status.DroneStatuses;
import org.uav.model.status.ProjectileStatuses;
import org.uav.queue.ControlMode;
import org.uav.scene.camera.Camera;

import java.util.concurrent.locks.ReentrantLock;

@Data
public class SimulationState {
    String assetsDirectory;
    String serverMap;
    String droneModelChecksum;
    final long window;
    float simulationTime;

    final DroneStatuses droneStatuses;
    final ReentrantLock droneStatusesMutex;
    final ProjectileStatuses projectileStatuses;
    final ReentrantLock projectileStatusesMutex;
    final Notifications notifications;

    final Camera camera;
    CameraMode currentCameraMode;
    Drone currentlyControlledDrone;
    ControlMode currentControlMode;
    final DroneStatuses currPassDroneStatuses;
    final ProjectileStatuses currPassProjectileStatuses;

    float lastHeartBeatTimeStamp;

    boolean mapOverlay;
    float mapZoom;
    Vector3f skyColor;

    Vector4f positionalModeDemands;
    Vector4f angleModeDemands;
    Vector4f acroModeDemands;


    public SimulationState(Config config, long window) {
        this.window = window;
        droneStatuses = new DroneStatuses();
        droneStatusesMutex = new ReentrantLock();
        projectileStatuses = new ProjectileStatuses();
        projectileStatusesMutex = new ReentrantLock();
        notifications = new Notifications();
        currentCameraMode = config.getDroneSettings().getDefaultCamera();
        currentControlMode = config.getDroneSettings().getDefaultControlMode();
        currentlyControlledDrone = null;
        currPassDroneStatuses = new DroneStatuses(droneStatuses.map);
        currPassProjectileStatuses = new ProjectileStatuses(projectileStatuses.map);
        camera = new Camera(this, config);
        mapOverlay = false;
        mapZoom = 1;
        skyColor = new Vector3f(0.529f, 0.808f, 0.922f);
        positionalModeDemands = null;
        angleModeDemands = null;
        acroModeDemands = null;
    }
}
