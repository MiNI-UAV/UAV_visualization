package org.uav.scene;

import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.uav.scene.shader.Shader;

@AllArgsConstructor
public class Fog {
    private final Vector3f color;
    private final float density;

    public void applyTo(Shader shader) {
        shader.use();
        shader.setVec3("fog.color", color);
        shader.setFloat("fog.density", density);
    }
}
