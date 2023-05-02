package org.uav.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallback;

import static java.lang.Math.*;
import static java.lang.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Camera {

    private Vector3f cameraPos = new Vector3f(0f,0f,0f).add(new Vector3f(-6.0f,0,-3.5f)) ;
    private final Vector3f cameraUp = new Vector3f(0, 0, -1f).normalize();
    private Vector3f cameraFront = new Vector3f(1, 0, 0);
    private final float fov = 90;

    // Movement
    private float yaw = (float) atan2(cameraFront.y, cameraFront.x), pitch = 0;
    private float lastX = 400, lastY = 300;
    private float movementSpeed = 2.5f;
    private float mouseSensitivity = 0.1f;
    private boolean firstMouse = true;
    private GLFWCursorPosCallback mouseCallback;

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

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .lookAt(cameraPos, new Vector3f(cameraPos).add(cameraFront), cameraUp);
    }

    public Vector3f getCameraFront() {
        return cameraFront;
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

    public void startProcessingMouseMovement(long window) {
        if (mouseCallback != null) return;
        mouseCallback = glfwSetCursorPosCallback(window, (window1, xpos, ypos) -> {
            if (firstMouse)
            {
                lastX = (float) xpos;
                lastY = (float) ypos;
                firstMouse = false;
            }

            float xoffset = (float) xpos - lastX;
            float yoffset = lastY - (float) ypos; // reversed since y-coordinates range from bottom to top
            lastX = (float) xpos;
            lastY = (float) ypos;

            float sensitivity = 0.1f;
            xoffset *= sensitivity;
            yoffset *= sensitivity;

            yaw += xoffset;
            pitch += yoffset;

            if (pitch > 89.0f)
                pitch = 89.0f;
            if (pitch < -89.0f)
                pitch = -89.0f;

            cameraFront.set(cos(toRadians(yaw)) * cos(toRadians(pitch)),
                    sin(toRadians(yaw)) * cos(toRadians(pitch)),
                    sin(toRadians(pitch))).normalize();
        });
    }

    public void stopProcessingMouseMovement(long window) {
        if (mouseCallback != null) {
            mouseCallback.close();
            glfwSetCursorPosCallback(window, null);
            mouseCallback = null;
        }
    }

}
