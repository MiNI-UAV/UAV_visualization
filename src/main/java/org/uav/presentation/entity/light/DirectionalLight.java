package org.uav.presentation.entity.light;

import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.uav.presentation.rendering.Shader;

@AllArgsConstructor
public class DirectionalLight {
    private final Vector3f lightDirection;
    private final Vector3f ambientComponent;
    private final Vector3f diffuseComponent;
    private final Vector3f specularComponent;

    public void applyTo(Shader shader) {
        shader.use();
        shader.setVec3("dirLight.direction", lightDirection);
        shader.setVec3("dirLight.ambient", ambientComponent);
        shader.setVec3("dirLight.diffuse", diffuseComponent);
        shader.setVec3("dirLight.specular", specularComponent);
    }
}
