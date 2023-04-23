package org.opengl.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;

import java.util.List;

public class ModelNode {

    private final String name;
    private final List<Mesh> meshes;
    private final List<ModelNode> children;

    private final Vector3f localTranslation;
    private final Quaternionf localRotation;
    private final Vector3f localScale;


    public ModelNode(String name, List<Mesh> meshes, List<ModelNode> children, Vector3f localTranslation, Quaternionf localRotation, Vector3f localScale) {
        this.name = name;
        this.meshes = meshes;
        this.children = children;
        this.localTranslation = localTranslation;
        this.localRotation = localRotation;
        this.localScale = localScale;
    }

    public void draw(MemoryStack stack, Shader shader, Vector3f parentTranslation, Quaternionf parentRotation, Vector3f parentScale) {
        Vector3f translation = new Vector3f().add(parentTranslation).add(localTranslation);
        Quaternionf rotation = new Quaternionf(0,0,0,1).mul(parentRotation).mul(localRotation);
        Vector3f scale = new Vector3f(parentScale.x * localScale.x, parentScale.y * localScale.y, parentScale.z * localScale.z);
        Matrix4f modelMatrix = new Matrix4f()
                .scale(scale)
                .translate(translation)
                .rotate(rotation);
        meshes.forEach(m -> m.draw(stack, shader, modelMatrix));
        children.forEach(n -> n.draw(stack, shader, translation, rotation, scale));

        System.out.printf("%-30s:  %10.5f,  %10.5f,  %10.5f,  %10.5f\n", name, rotation.x, rotation.y, rotation.z, rotation.w);
        //System.out.println(name + " scale: \t\t\t" + rotation.x + ", " + rotation.y + ", " + rotation.z + ", " + rotation.w);
    }
}
