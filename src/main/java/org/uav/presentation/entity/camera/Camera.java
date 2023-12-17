package org.uav.presentation.entity.camera;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.uav.logic.config.Config;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.utils.Convert;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private static final Vector3f CAMERA_UP = new Vector3f(0, 0, -1f);
    private final Vector3f cameraFPP;
    private final Vector3f cameraTPP;
    private final SimulationState simulationState;
    @Getter @Setter
    private Vector3f cameraPos;
    @Setter @Getter
    private Vector3f cameraUp;
    @Setter @Getter
    private Vector3f cameraFront;
    @Getter @Setter
    private Vector3f cameraVel;
    @Getter @Setter
    private float fov;

    // Free Camera Movement
    private float yaw;
    private float pitch;
    private final float movementSpeed;
    private float deltaTime;
    private float lastTime;

    public Camera(SimulationState simulationState, Config config) {
        this.simulationState = simulationState;
        fov = config.getGraphicsSettings().getFov();
        cameraFPP = new Vector3f(ArrayUtils.toPrimitive(config.getSceneSettings().getCameraFPP(), 0.0F));
        cameraTPP = new Vector3f(ArrayUtils.toPrimitive(config.getSceneSettings().getCameraTPP(), 0.0F));
        cameraPos = new Vector3f();
        cameraUp = CAMERA_UP;
        cameraFront = new Vector3f();
        cameraVel = new Vector3f();
        yaw = (float) atan2(cameraFront.y, cameraFront.x);
        pitch = 0;
        movementSpeed = 2.5f;
        deltaTime = 0f;
        lastTime = 0f;
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .lookAt(cameraPos, new Vector3f(cameraPos).add(cameraFront), cameraUp);
    }

    public void updateCamera() {
        var drone = simulationState.getPlayerDrone();
        var dronePosition = drone.isPresent() ? drone.get().droneStatus.position : new Vector3f();
        var droneRotation = drone.isPresent() ? drone.get().droneStatus.rotation : new Quaternionf();
        var droneVelocity = drone.isPresent() ? drone.get().droneStatus.linearVelocity : new Vector3f();
        float currTime = simulationState.getSimulationTimeS();
        deltaTime = currTime - lastTime;
        lastTime = currTime;
        switch(simulationState.getCurrentCameraMode()) {
            case DroneCamera -> {
                var cameraOffset = new Vector3f(cameraTPP);
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraVel(new Vector3f(droneVelocity));
                setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                setCameraUp(CAMERA_UP);
            }
            case FreeCamera -> {
                setCameraUp(CAMERA_UP);
                updateFreeCamera(simulationState.getWindow(), deltaTime);
            }
            case RacingCamera -> {
                var rot = new Quaternionf(droneRotation);
                var cameraOffset = new Vector3f(cameraTPP).rotate(rot);
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraVel(new Vector3f(droneVelocity));
                setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                setCameraUp(new Vector3f(CAMERA_UP).rotate(rot));
            }
            case HorizontalCamera -> {
                var rot = Convert.toEuler(droneRotation);
                rot.x = 0;
                rot.y = 0;
                var cameraOffset = new Vector3f(cameraTPP).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraVel(new Vector3f(droneVelocity));
                setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                setCameraUp(CAMERA_UP);
            }
            case ObserverCamera -> {
                updateFreeCamera(simulationState.getWindow(), deltaTime);
                setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                setCameraUp(CAMERA_UP);
                setCameraVel(new Vector3f());
            }
            case SoftFPV -> {
                var rot = Convert.toEuler(droneRotation);
                rot.x = 0;
                rot.y = 0;
                var cameraOffset = new Vector3f(cameraFPP).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraVel(new Vector3f(droneVelocity));
                setCameraFront(cameraOffset.normalize());
                setCameraUp(CAMERA_UP);
            }
            case HardFPV -> {
                var rot = new Quaternionf(droneRotation);
                var cameraOffset = new Vector3f(cameraFPP).rotate(rot);
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                setCameraPos(cameraPos);
                setCameraVel(new Vector3f(droneVelocity));
                setCameraFront(cameraOffset.normalize());
                setCameraUp(new Vector3f(CAMERA_UP).rotate(rot));
            }
        }

    }

    public void updateFreeCamera(long window, float deltaTime) {
        float cameraSpeed = movementSpeed * deltaTime; // adjust accordingly
        float multiplier = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS ? 2 : 1;

        var deltaMovement = new Vector3f();
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
            deltaMovement = new Vector3f(cameraFront).mul(cameraSpeed).mul(multiplier);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
            deltaMovement = new Vector3f(cameraFront).mul(cameraSpeed).mul(multiplier).negate();
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
            deltaMovement = new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed).mul(multiplier).negate();
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
            deltaMovement = new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed).mul(multiplier);

        cameraPos.add(deltaMovement);
        setCameraVel(new Vector3f(deltaMovement));

        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS)
            pitch += 0.03f * multiplier;
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS)
            pitch -= 0.03f * multiplier;
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS)
            yaw -= 0.03f * multiplier;
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS)
            yaw += 0.03f * multiplier;

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
