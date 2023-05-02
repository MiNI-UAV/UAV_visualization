package org.uav.model;


import org.joml.Vector3f;

public class Material {
    private final Vector3f diffuse;
    public float roughness;
    public float metallic;

    public Vector3f getDiffuse() {
        return diffuse;
    }

    public Material(Vector3f diffuse, float roughness, float metallic) {
        this.diffuse = diffuse;
        this.roughness = roughness;
        this.metallic = metallic;
    }
}
