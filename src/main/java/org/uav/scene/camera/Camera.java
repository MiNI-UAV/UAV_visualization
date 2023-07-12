package org.uav.scene.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.uav.model.SimulationState;
import org.uav.utils.Convert;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Camera {
    private static final Vector3f CAMERA_UP = new Vector3f(0, 0, -1f);
    private static final Vector3f CAMERA_FP = new Vector3f(1.0f,0f,0.1f);
    private static final Vector3f CAMERA_TP = new Vector3f(-3.0f,0,-1.5f);
    private final SimulationState simulationState;
    private Vector3f cameraPos = new Vector3f();
    private Vector3f cameraUp = CAMERA_UP;
    private Vector3f cameraFront = new Vector3f();
    private static final float fov = 75;

    // Free Camera Movement
    private float yaw = (float) atan2(cameraFront.y, cameraFront.x), pitch = 0;
    private static final float movementSpeed = 2.5f;
    static float deltaTime = 0.0f;
    static float lastTime = 0.0f;

    public Camera(SimulationState simulationState) {
        this.simulationState = simulationState;
    }

    public Vector3f getCameraPos() {
        return cameraPos;
    }

    public void setCameraPos(Vector3f cameraPos) {
        this.cameraPos = cameraPos;
    }

    public void setCameraFront(Vector3f cameraFront) {
        this.cameraFront = cameraFront;
    }

    public void setCameraUp(Vector3f cameraUp) {
        this.cameraUp = cameraUp;
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .lookAt(cameraPos, new Vector3f(cameraPos).add(cameraFront), cameraUp);
    }

    public float getFov() {
        return fov;
    }

    public void updateCamera() {
        Vector3f dronePosition, droneRotation;
        if(simulationState.getDroneStatuses().map.containsKey(simulationState.getCurrentlyControlledDrone().id)) {
            dronePosition = simulationState.getDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().id).position;
            droneRotation = simulationState.getDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().id).rotation;
        }
        else {
            dronePosition = new Vector3f();
            droneRotation = new Vector3f();
        }
        float currTime = (float) glfwGetTime();
        deltaTime = currTime - lastTime;
        lastTime = currTime;
        switch(simulationState.getCurrentCameraMode()) {
            case DroneCamera -> {
                var cameraOffset = new Vector3f(CAMERA_TP);
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                setCameraUp(CAMERA_UP);
            }
            case FreeCamera -> {
                setCameraUp(CAMERA_UP);
                updateFreeCamera(simulationState.getWindow(), deltaTime);
            }
            case RacingCamera -> {
                var rot = new Vector3f(droneRotation);
                var cameraOffset = new Vector3f(CAMERA_TP).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                setCameraUp(new Vector3f(CAMERA_UP).rotate(Convert.toQuaternion(rot)));
            }
            case HorizontalCamera -> {
                var rot = new Vector3f(droneRotation);
                rot.x = 0;
                rot.y = 0;
                var cameraOffset = new Vector3f(CAMERA_TP).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                setCameraUp(CAMERA_UP);
            }
            case ObserverCamera -> {
                updateFreeCamera(simulationState.getWindow(), deltaTime);
                setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                setCameraUp(CAMERA_UP);
            }
            //TODO: SoftFPV is not implemented yet, now its copy of hardFPV
            case HardFPV, SoftFPV  -> {
                var rot = new Vector3f(droneRotation);
                var cameraOffset = new Vector3f(CAMERA_FP).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraFront(cameraOffset.normalize());
                setCameraUp(new Vector3f(CAMERA_UP).rotate(Convert.toQuaternion(rot)));
            }
        }
    }

    public void updateFreeCamera(long window, float deltaTime) {
        float cameraSpeed = movementSpeed * deltaTime; // adjust accordingly
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            cameraPos.add(new Vector3f(cameraFront).mul(cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            cameraPos.sub(new Vector3f(cameraFront).mul(cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            cameraPos.sub(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            cameraPos.add(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));

        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS)
            pitch += 0.015;
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS)
            pitch -= 0.015;
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS)
            yaw -= 0.015;
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS)
            yaw += 0.015;

        if (pitch > PI/2)
            pitch = (float)PI/2 -0.01f;
        if (pitch < -PI/2)
            pitch = -(float)PI/2 +0.01f;

        cameraFront.set(
                new Vector3f(
                        (float) (cos(yaw) * cos(pitch)),
                        (float) (sin(yaw) * cos(pitch)),
                        (float) sin(pitch)
                )
        );
    }
}
