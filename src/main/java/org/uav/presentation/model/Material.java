package org.uav.presentation.model;


import lombok.Value;
import org.joml.Vector4f;

@Value
public class Material {
    Vector4f albedo;
    float normalScale;
    float roughness;
    float metallic;
    float aoStrength;
}
