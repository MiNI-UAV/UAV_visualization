package org.uav.model;


import lombok.Value;
import org.joml.Vector3f;
import org.joml.Vector4f;

@Value
public class Material {
    Vector4f baseColor;
    Vector3f diffuse;
    float roughness;
    float metallic;
}
