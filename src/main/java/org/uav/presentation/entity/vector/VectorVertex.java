package org.uav.presentation.entity.vector;

import org.joml.Vector2f;
import org.uav.presentation.rendering.ShaderVertex;

import java.nio.FloatBuffer;

public class VectorVertex extends ShaderVertex {
    public static int NUMBER_OF_FLOATS = 2;
    private final Vector2f position2d;

    public VectorVertex(Vector2f position2d) {
        this.position2d = position2d;
    }

    public VectorVertex(float x, float y) {
        this.position2d = new Vector2f(x, y);
    }

    @Override
    public void insertIntoFloatBuffer(FloatBuffer fb) {
        fb.put(position2d.x);
        fb.put(position2d.y);
    }

    @Override
    public int getNumberOfFloats() {
        return NUMBER_OF_FLOATS;
    }
}
