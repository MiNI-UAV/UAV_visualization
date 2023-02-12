package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.Model;
import org.opengl.shader.Shader;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class DrawableLightOfGabriel implements Drawable {

    private final Model objectModel;

    public DrawableLightOfGabriel(Model model) {
        objectModel = model;
    }

    @Override
    public void draw(MemoryStack stack, Shader shader) {
        shader.use();
        var model = new Matrix4f()
                .translate(getPosition())
                .scale(.02f,.02f,.02f);
        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("lightColor",  new Vector3f(1f, 0.1f, 0.1f));
        objectModel.draw(shader);

        model = new Matrix4f()
                .translate(getPosition())
                .scale(.02f,.02f,.02f);
        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("lightColor",  new Vector3f(0.1f, 1f, 0.1f));
        objectModel.draw(shader);

        model = new Matrix4f()
                .translate(getPosition())
                .scale(.02f,.02f,.02f);
        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("lightColor",  new Vector3f(0.1f, 0.1f, 1f));
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
