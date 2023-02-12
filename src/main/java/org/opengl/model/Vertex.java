package org.opengl.model;


import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

public class Vertex {

    public static int NUMBER_OF_FLOATS = 8;
    private Vector3f position;
    private Vector3f normal;
    private Vector2f texCoords;

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public void setNormal(Vector3f normal) {
        this.normal = normal;
    }

    public Vector2f getTexCoords() {
        return texCoords;
    }

    public void setTexCoords(Vector2f texCoords) {
        this.texCoords = texCoords;
    }

    public Vertex(Vector3f position, Vector3f normal, Vector2f texCoords) {
        this.position = position;
        this.normal = normal;
        this.texCoords = texCoords;
    }

    public void insertIntoFloatBuffer(FloatBuffer fb) {
        fb.put(position.x);
        fb.put(position.y);
        fb.put(position.z);
        fb.put(normal.x);
        fb.put(normal.y);
        fb.put(normal.z);
        fb.put(texCoords.x);
        fb.put(texCoords.y);
    }
}
