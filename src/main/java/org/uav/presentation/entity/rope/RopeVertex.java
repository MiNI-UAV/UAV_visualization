package org.uav.presentation.entity.rope;

import org.uav.presentation.rendering.ShaderVertex;

import java.nio.FloatBuffer;

public class RopeVertex extends ShaderVertex {
    public static int NUMBER_OF_FLOATS = 2;
    private final float t;
    private final float tNext;

    public RopeVertex(float t, float tNext) {
        this.t = t;
        this.tNext = tNext;
    }

    @Override
    public void insertIntoFloatBuffer(FloatBuffer fb) {
        fb.put(t);
        fb.put(tNext);
    }

    @Override
    public int getNumberOfFloats() {
        return NUMBER_OF_FLOATS;
    }
}
