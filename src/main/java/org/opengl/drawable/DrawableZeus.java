package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.Model;
import org.opengl.shader.Shader;

import static org.joml.Math.*;
import static org.joml.Math.cos;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class DrawableZeus implements Drawable {

    private final Model objectModel;

    public DrawableZeus(Model model) {
        objectModel = model;
    }
    
    @Override
    public void draw(MemoryStack stack, Shader shader) {
        shader.use();
        Matrix4f model = new Matrix4f()
                .translate(getPosition())
                .rotate(toRadians(135.0f), new Vector3f(0f, 1f, 0f));
        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("material.ambient", new Vector3f(1.0f, 1.0f, 1.0f));
        shader.setVec3("material.diffuse", new Vector3f(0.8f, 0.8f, 0.8f));
        shader.setVec3("material.specular", new Vector3f(1.f, 1.f, 1.f));
        shader.setFloat("material.shininess", 64.0f);
        shader.setBool("useDirectionalLight", true);
        objectModel.draw(shader);
    }
    @Override
    public Vector3f getPosition()  {
        return new Vector3f(
                0.05f*sin(-(float)glfwGetTime()),
                -0.01f* sin((float)-glfwGetTime()) + (float)(-glfwGetTime()%100) * 0.001f*cos((float)glfwGetTime()) - (float)(100-glfwGetTime()%100) * 0.001f*cos((float)-glfwGetTime()),
                0.05f*cos((float)-glfwGetTime()));
    }
    
}
