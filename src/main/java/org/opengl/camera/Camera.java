package org.opengl.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallback;

import static java.lang.Math.*;
import static java.lang.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Camera {

    private Vector3f cameraPos = new Vector3f(0f,0f,-7f);
    private final Vector3f cameraUp = new Vector3f(0, 1, 0);
    private Vector3f cameraFront = new Vector3f(0, 0, 1);
    private final float fov = 45;

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

}
