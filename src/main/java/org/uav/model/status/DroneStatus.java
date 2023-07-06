package org.uav.model.status;

import org.joml.Vector3f;

import java.util.Vector;

public class DroneStatus {
    public int id;
    public float time;
    public Vector3f position;
    public Vector3f rotation;
    public Vector3f linearVelocity;
    public Vector3f angularVelocity;
    public Vector<Float> propellers;

    public DroneStatus() {
        id = 0;
        position = new Vector3f();
        rotation = new Vector3f();
        linearVelocity = new Vector3f();
        angularVelocity = new Vector3f();
        propellers = new Vector<>();
    }

    public int getId() {
        return id;
    }
}
