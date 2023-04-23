package org.opengl.model;

import org.joml.Vector3f;

public class DroneStatus {
    public Vector3f position;
    public Vector3f rotation;

    public DroneStatus() {
        position = new Vector3f();
        rotation = new Vector3f();
    }
    public DroneStatus(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }
}
