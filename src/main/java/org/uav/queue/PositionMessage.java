package org.uav.queue;

import org.joml.Vector3f;

public class PositionMessage {

    public Vector3f position;
    public Vector3f rotation;

    public PositionMessage(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }
}
