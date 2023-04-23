package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.ModelOld;
import org.opengl.shader.Shader;

import static org.joml.Math.cos;

public class DrawableCupcake extends Drawable {

    private final ModelOld objectModel;

    public DrawableCupcake(ModelOld model) {
        objectModel = model;
    }

    @Override
    public void draw(MemoryStack stack, Shader shader) {
        shader.use();
        var model = new Matrix4f()
                .translate(getPosition())
                .scale(.05f,.05f,.05f);
        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("material.ambient", new Vector3f(1.0f, 1.0f, 1.0f));
        shader.setVec3("material.diffuse", new Vector3f(0.7f, 0.7f, 0.7f));
        shader.setVec3("material.specular", new Vector3f(0.1f, 0.1f, 0.1f));
        shader.setFloat("material.shininess", 16.0f);
        shader.setBool("useDirectionalLight", false);
        objectModel.draw(stack, shader, model);
    }

    @Override
    public void draw(Vector3f parentTranslation, Vector3f parentScaling, MemoryStack stack, Shader shader) {

    }


    @Override
    public Vector3f getPosition()  {
        return new Vector3f(1f,-1f,-3f);
    }

}
