package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.config.Configuration;
import org.opengl.model.Model;
import org.opengl.shader.Shader;

import java.util.Random;

import static org.joml.Math.*;
import static org.joml.Math.cos;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class DrawableJupiter implements Drawable {

    private final Model objectModel;
    private final Random random;

    private final Configuration configuration;

    public DrawableJupiter(Model model, Configuration configuration) {
        objectModel = model;
        random = new Random();
        this.configuration = configuration;
    }

    @Override
    public void draw(MemoryStack stack, Shader shader) {
        shader.use();
        Matrix4f model = new Matrix4f()
                .translate(getPosition())
                .scale(.3f,.3f,.3f)
                .rotate((float)glfwGetTime() * toRadians(20.0f), new Vector3f(0f, 1f, 0f));
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
                1.25f*sin((float)glfwGetTime()) + random.nextFloat() * configuration.shakeFactor - configuration.shakeFactor/2f,
                .5f + -0.5f* sin((float)glfwGetTime()) + random.nextFloat() * configuration.shakeFactor - configuration.shakeFactor/2f,
                1.25f*cos((float)glfwGetTime()) + random.nextFloat() * configuration.shakeFactor - configuration.shakeFactor/2f);
    }
}
