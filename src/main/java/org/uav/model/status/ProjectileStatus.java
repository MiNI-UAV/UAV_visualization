package org.uav.model.status;

import org.joml.Vector3f;

public class ProjectileStatus {
    public int id;
    public Vector3f position;
    public Vector3f velocity;

    public ProjectileStatus(int id, Vector3f position, Vector3f velocity){
        this.id = id;
        this.position = position;
        this.velocity = velocity;
    }

    public int getId() {
        return id;
    }
}
