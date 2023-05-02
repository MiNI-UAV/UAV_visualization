package org.uav.status;

import org.joml.Vector3f;

import java.util.Vector;

public class DroneStatus {
    public Vector3f position;
    public Vector3f rotation;
    public Vector<Float> propellers;

    public DroneStatus() {
        position = new Vector3f();
        rotation = new Vector3f();
        propellers = new Vector<>();
    }
    public DroneStatus(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }
}
