package org.opengl.model;


import org.joml.Vector3f;
import org.joml.Vector4f;

public class Material {

    public static final Material DEFAULT_MATERIAL = new Material(new Vector3f());
    private Vector3f diffuse;

    public Vector3f getDiffuse() {
        return diffuse;
    }

    public Material(Vector3f diffuse) {
        this.diffuse = diffuse;
    }
}
