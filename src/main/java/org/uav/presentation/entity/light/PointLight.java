package org.uav.presentation.entity.light;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.joml.Vector3f;
import org.uav.presentation.rendering.Shader;

@AllArgsConstructor
public class PointLight {
    @Setter
    private Vector3f position;
    private Vector3f ambientComponent;
    private Vector3f diffuseComponent;
    private Vector3f specularComponent;
    private float constantDecay;
    private float linearDecay;
    private float quadraticDecay;

    public void applyTo(Shader shader) {
        shader.use();
        shader.setVec3("cameraPointLight.position", position);
        shader.setVec3("cameraPointLight.ambient", ambientComponent);
        shader.setVec3("cameraPointLight.diffuse", diffuseComponent);
        shader.setVec3("cameraPointLight.specular", specularComponent);
        shader.setFloat("cameraPointLight.constant", constantDecay);
        shader.setFloat("cameraPointLight.linear", linearDecay);
        shader.setFloat("cameraPointLight.quadratic", quadraticDecay);
    }

    public static class PointLightFactory {
        public static PointLight createDronePointLight() {
            return new PointLight(new Vector3f(), new Vector3f(0.4f), new Vector3f(0.05f), new Vector3f(0.05f), 1.0f, 0.045f, .0075f);
        }
    }
}
