package org.uav.logic.state.drone;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Vector;

public class DroneStatus {
    public int id;
    public float time;
    public Vector3f position;
    public Quaternionf rotation;
    public Vector3f linearVelocity;
    public Vector3f angularVelocity;
    public Vector<Float> propellersRadps;

    public DroneStatus() {
        id = 0;
        position = new Vector3f();
        rotation = new Quaternionf();
        linearVelocity = new Vector3f();
        angularVelocity = new Vector3f();
        propellersRadps = new Vector<>();
    }
}
