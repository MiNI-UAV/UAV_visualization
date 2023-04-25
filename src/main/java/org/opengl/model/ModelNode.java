package org.opengl.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;

import java.util.List;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class ModelNode {

    private final String name;
    private final List<Mesh> meshes;
    private final List<ModelNode> children;

//    public Vector3f localTranslation;
//    public Quaternionf localRotation;
//    public Vector3f localScale;
    public Matrix4f customTransform;
    private  final Matrix4f localTransform;


    public ModelNode(String name, List<Mesh> meshes, List<ModelNode> children, Vector3f localTranslation, Quaternionf localRotation, Vector3f localScale, Matrix4f localTransform) {
        this.name = name;
        this.meshes = meshes;
        this.children = children;
        //this.localTranslation = localTranslation;
        //this.localRotation = localRotation;
        //this.localScale = localScale;
        this.localTransform = localTransform;
        this.customTransform = new Matrix4f();
    }

    public void draw(MemoryStack stack, Shader shader/*, Vector3f parentTranslation, Quaternionf parentRotation, Vector3f parentScale*/, Matrix4f parentTransform) {
        if(name.equals("Drone_Turb_Blade_L_body_0") || name.equals("Drone_Turb_Blade_R_body_0"))
            customTransform = new Matrix4f().rotate((float)glfwGetTime() * toRadians(-1000.0f), new Vector3f(0f, 1f, 0f));
        Matrix4f transform = new Matrix4f(parentTransform);
        Matrix4f temp = new Matrix4f(localTransform);
        Matrix4f catchingUpTransform = new Matrix4f(transform).mul(customTransform).mul(temp);// TODO tutaj było odwrotnie dwa pierwsze
        //Vector3f translation = new Vector3f().add(parentTranslation).add(localTranslation);
        //Quaternionf rotation = new Quaternionf().mul(parentRotation).mul(localRotation);
        //Vector3f scale = new Vector3f(parentScale.x * localScale.x, parentScale.y * localScale.y, parentScale.z * localScale.z);
//        Matrix4f modelMatrix = new Matrix4f()
//                .scale(localScale)
//                .rotate(localRotation)
//                .translate(localTranslation);
//        Matrix4f result = new Matrix4f(transform).mul(modelMatrix);
        meshes.forEach(m -> m.draw(stack, shader, catchingUpTransform));
        children.forEach(n -> n.draw(stack, shader/*, translation, rotation, scale*/, catchingUpTransform));

        //System.out.printf("%-30s:  %10.5f,  %10.5f,  %10.5f,  %10.5f\n", name, rotation.x, rotation.y, rotation.z, rotation.w);
        //System.out.println(name + " scale: \t\t\t" + rotation.x + ", " + rotation.y + ", " + rotation.z + ", " + rotation.w);
    }
}
