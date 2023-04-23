package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.ModelOld;
import org.opengl.shader.Shader;

import java.util.Collections;
import java.util.List;

public class DrawableEnvironment {
    private final ModelOld objectModel;

    private final List<DrawablePropellers> childrenDrawables;

    public Vector3f position;
    public Vector3f rotation;

    public DrawableEnvironment(ModelOld objectModel) {
        this.objectModel = objectModel;
        this.childrenDrawables = Collections.emptyList();
        position = new Vector3f(-1,-1,10);
        rotation = new Vector3f(2*(float) Math.PI/4, 0,0);
    }

    public void draw(MemoryStack stack, Shader shader) {
        shader.use();

        var model = new Matrix4f()
                .translate(getPosition())
                .rotate(rotation.x, new Vector3f(1f, 0f, 0f))
                .rotate(rotation.z, new Vector3f(0f, 1f, 0f))
                .rotate(rotation.y, new Vector3f(0f, 0f, 1f))
                .scale(1f);


        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("material.ambient", new Vector3f(1.0f, 1.0f, 1.0f));
        shader.setVec3("material.diffuse", new Vector3f(0.7f, 0.7f, 0.7f));
        shader.setVec3("material.specular", new Vector3f(0.1f, 0.1f, 0.1f));
        shader.setFloat("material.shininess", 16.0f);
        shader.setBool("useDirectionalLight", true);
        objectModel.draw(stack, shader, model);
    }

    public void draw(Vector3f parentTranslation, Vector3f parentScaling, MemoryStack stack, Shader shader) {

    }

    public Vector3f getPosition()  {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }
}
