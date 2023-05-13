package org.uav.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static java.lang.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Camera {

    private Vector3f cameraPos = new Vector3f(10f,10f,-10f);//.add(new Vector3f(-6.0f,0,-3.5f)) ;
    private Vector3f cameraUp = new Vector3f(0, 0, -1f).normalize();
    private Vector3f cameraFront = new Vector3f(-1, -1, 1).normalize();
    private static final float fov = 90;

    // Movement
    private float yaw = (float) atan2(cameraFront.y, cameraFront.x), pitch = 0;
    private static final float movementSpeed = 2.5f;

    public Camera() {}

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

    public void processInput(long window, float deltaTime) {
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
