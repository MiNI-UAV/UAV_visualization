package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.Model;
import org.opengl.shader.Shader;

import static org.joml.Math.*;
import static org.joml.Math.cos;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class DrawablePointyHand implements Drawable {

    private final Model objectModel;

    public DrawablePointyHand(Model model) {
        objectModel = model;
    }

    @Override
    public void draw(MemoryStack stack, Shader shader) {
        shader.use();
        var model = new Matrix4f()
                .translate(getPosition())
                .scale(.3f,.3f,.3f)
                .rotate(toRadians(90), new Vector3f(1f, 0f, 0f))
                .rotate((float)glfwGetTime(), new Vector3f(0f, 0f, -1f));
        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("material.ambient", new Vector3f(1.0f, 1.0f, 1.0f));
        shader.setVec3("material.diffuse", new Vector3f(0.7f, 0.7f, 0.7f));
        shader.setVec3("material.specular", new Vector3f(0.1f, 0.1f, 0.1f));
        shader.setFloat("material.shininess", 16.0f);
        shader.setBool("useDirectionalLight", false);
        objectModel.draw(shader);
    }

    @Override
    public Vector3f getPosition()  {
        return new Vector3f(
                -2.25f*sin((float)glfwGetTime()),
                0.1f*sin((float)(PI*glfwGetTime())),
                -2.25f*cos((float)glfwGetTime()));
    }

}
