package org.opengl.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;
import de.javagl.jgltf.model.*;

import java.util.List;

import static java.lang.Math.cos;
import static org.joml.Math.sin;
import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class ModelNode {

    private final String name;
    private final List<Mesh> meshes;
    private final List<ModelNode> children;

    public Vector3f localTranslation;
    public Quaternionf localRotation;
    public Vector3f localScale;
    public Vector3f customTranslation;
    public Quaternionf customRotation;
    public Vector3f customScale;


    public ModelNode(String name, List<Mesh> meshes, List<ModelNode> children, Vector3f localTranslation, Quaternionf localRotation, Vector3f localScale) {
        this.name = name;
        this.meshes = meshes;
        this.children = children;
        this.localTranslation = localTranslation;
        this.localRotation = localRotation;
        this.localScale = localScale;
        this.customTranslation = new Vector3f();
        this.customRotation = new Quaternionf();
        this.customScale = new Vector3f(1);
    }

    public void draw(MemoryStack stack, Shader shader, Matrix4f parentTransform) {
        if(name.equals("Drone_Turb_Blade_L_body_0") || name.startsWith("propeller"))
            customRotation = new Quaternionf(0,1 * sin(glfwGetTime()*100),0,-cos(glfwGetTime()*100)).normalize();
        if(name.equals("Drone_Turb_Blade_R_body_0"))
            customRotation = new Quaternionf(0,1 * sin(glfwGetTime()*100),0,cos(glfwGetTime()*100)).normalize();
        Vector3f translation = new Vector3f().add(localTranslation).add(customTranslation);
        Quaternionf rotation = new Quaternionf().mul(localRotation).mul(customRotation);
        Vector3f scale = new Vector3f(localScale.x * customScale.x, localScale.y * customScale.y, localScale.z * customScale.z);

        Matrix4f localTransformation = new Matrix4f()
                .translate(translation)
                .rotate(rotation)
                .scale(scale);
        Matrix4f globalTransformation = new Matrix4f(parentTransform).mul(localTransformation);

        meshes.forEach(m -> m.draw(stack, shader, globalTransformation));
        children.forEach(n -> n.draw(stack, shader, globalTransformation));
    }
}
