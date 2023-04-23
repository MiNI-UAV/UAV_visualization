package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.ModelOld;
import org.opengl.shader.Shader;

public class DrawableCandleLight extends Drawable {

    private final ModelOld objectModel;

    public DrawableCandleLight(ModelOld model) {
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
        objectModel.draw(stack, shader, model);
    }

    @Override
    public void draw(Vector3f parentTranslation, Vector3f parentScaling, MemoryStack stack, Shader shader) {

    }


    @Override
    public Vector3f getPosition()  {
        return new Vector3f(1f,-0.9f,-3f);
    }

}
