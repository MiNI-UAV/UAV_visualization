package org.uav.presentation.entity.skybox;
import org.joml.Vector3f;
import org.uav.presentation.rendering.ShaderVertex;

import java.nio.FloatBuffer;

public class SkyboxVertex extends ShaderVertex {
    public static int NUMBER_OF_FLOATS = 3;
    private final Vector3f position;

    public SkyboxVertex(float x, float y, float z) {
        this.position = new Vector3f(x, y, z);
    }

    @Override
    public void insertIntoFloatBuffer(FloatBuffer fb) {
        fb.put(position.x);
        fb.put(position.y);
        fb.put(position.z);
    }

    @Override
    public int getNumberOfFloats() {
        return NUMBER_OF_FLOATS;
    }
}
