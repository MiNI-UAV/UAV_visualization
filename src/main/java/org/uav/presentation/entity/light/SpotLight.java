package org.uav.presentation.entity.light;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.joml.Vector3f;
import org.uav.presentation.rendering.Shader;

import static org.joml.Math.cos;
import static org.joml.Math.toRadians;

@AllArgsConstructor
public class SpotLight {
    @Setter
    private boolean spotLightOn;
    @Setter
    private Vector3f position;
    @Setter
    private Vector3f direction;
    private float innerCutOff;
    private float outerCutOff;
    private Vector3f ambientComponent;
    private Vector3f diffuseComponent;
    private Vector3f specularComponent;
    private float constantDecay;
    private float linearDecay;
    private float quadraticDecay;

    public void applyTo(Shader shader) {
        shader.use();
        shader.setBool("spotLightOn",  spotLightOn);
        shader.setVec3("spotLight.position", position);
        shader.setVec3("spotLight.direction", direction);
        shader.setFloat("spotLight.cutOff", innerCutOff);
        shader.setFloat("spotLight.outerCutOff", outerCutOff);
        shader.setVec3("spotLight.ambient", ambientComponent);
        shader.setVec3("spotLight.diffuse", diffuseComponent);
        shader.setVec3("spotLight.specular", specularComponent);
        shader.setFloat("spotLight.constant", constantDecay);
        shader.setFloat("spotLight.linear", linearDecay);
        shader.setFloat("spotLight.quadratic", quadraticDecay);
    }
    public static class SpotlightFactory {
        public static SpotLight createDroneSpotlight() {
            return new SpotLight(
                    false,
                    new Vector3f(),
                    new Vector3f(),
                    cos(toRadians(40f)),
                    cos(toRadians(45.5f)),
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    1.0f,
                    0.027f,
                    0.0028f
            );
        }
    }

}
