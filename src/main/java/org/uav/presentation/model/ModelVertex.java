package org.uav.presentation.model;


import org.joml.Vector2f;
import org.joml.Vector3f;
import org.uav.presentation.rendering.ShaderVertex;

import java.nio.FloatBuffer;

public class ModelVertex extends ShaderVertex {
    public static int NUMBER_OF_FLOATS = 8;
    private final Vector3f position;
    private final Vector3f normal;
    private final Vector2f texCoords;

    public ModelVertex(Vector3f position, Vector3f normal, Vector2f texCoords) {
        this.position = position;
        this.normal = normal;
        this.texCoords = texCoords;
    }

    @Override
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

    @Override
    public int getNumberOfFloats() {
        return NUMBER_OF_FLOATS;
    }
}
