package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.Model;
import org.opengl.shader.Shader;

public class DrawableCandleLight implements Drawable {

    private final Model objectModel;

    public DrawableCandleLight(Model model) {
        objectModel = model;
    }

    @Override
    public void draw(MemoryStack stack, Shader shader) {
        shader.use();
        var model = new Matrix4f()
                .translate(getPosition())
                .scale(.01f,.01f,.01f);
        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("lightColor", new Vector3f(1.f, 	1.f, 1.f));
        objectModel.draw(shader);
    }

    @Override
    public Vector3f getPosition()  {
        return new Vector3f(1f,-0.9f,-3f);
    }

}
