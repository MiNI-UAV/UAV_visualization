package org.uav.status;

import org.joml.Vector3f;

import java.util.Vector;

public class DroneStatus {
    public Vector3f position;
    public Vector3f rotation;
    public Vector3f linearVelocity;
    public Vector3f angularVelocity;
    public Vector<Float> propellers;

    public DroneStatus() {
        position = new Vector3f();
        rotation = new Vector3f();
        linearVelocity = new Vector3f();
        angularVelocity = new Vector3f();
        propellers = new Vector<>();
    }
}
