package org.uav.presentation.entity.sprite;

import org.joml.Vector2f;
import org.uav.presentation.rendering.ShaderVertex;

import java.nio.FloatBuffer;

public class SpriteVertex extends ShaderVertex {
    public static int NUMBER_OF_FLOATS = 4;
    private final Vector2f position2d;
    private final Vector2f texCoords;

    public SpriteVertex(Vector2f position2d, Vector2f texCoords) {
        this.position2d = position2d;
        this.texCoords = texCoords;
    }

    @Override
    public void insertIntoFloatBuffer(FloatBuffer fb) {
        fb.put(position2d.x);
        fb.put(position2d.y);
        fb.put(texCoords.x);
        fb.put(texCoords.y);
    }

    @Override
    public int getNumberOfFloats() {
        return NUMBER_OF_FLOATS;
    }
}
