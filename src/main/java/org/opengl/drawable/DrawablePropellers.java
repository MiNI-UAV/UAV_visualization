package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.ModelOld;
import org.opengl.shader.Shader;

import java.util.Collections;
import java.util.List;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class DrawablePropellers {

    private final ModelOld objectModel;
    private final List<Drawable> childrenModels;

    public DrawablePropellers(ModelOld model) {
        objectModel = model;
        childrenModels = Collections.emptyList();
    }
    public void draw(MemoryStack stack, Shader shader) {

    }

    public void draw(Vector3f parentTranslation, Vector3f parentRotation, Vector3f parentScaling, MemoryStack stack, Shader shader) {
//
//        var v1 = objectModel.meshes.get(0).localTransformation.m30();
//        var v2 = objectModel.meshes.get(0).localTransformation.m31();
//        var v3 = objectModel.meshes.get(0).localTransformation.m32();
//        var v4 = objectModel.meshes.get(0).localTransformation.m33();

        shader.use();
        var model = new Matrix4f()
                .translate(parentTranslation)
                .rotate(parentRotation.x, new Vector3f(1f, 0f, 0f))
                .rotate(parentRotation.z, new Vector3f(0f, 1f, 0f))
                .rotate(parentRotation.y, new Vector3f(0f, 0f, 1f))
//                .translate(new Vector3f(v1, v2, v3).mul(parentScaling))
                .scale(parentScaling)
                .rotate((float)glfwGetTime() * toRadians(1000.0f), new Vector3f(0f, 1f, 0f));

        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("material.ambient", new Vector3f(0.1f, 0.1f, 0.1f));
        shader.setVec3("material.diffuse", new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setVec3("material.specular", new Vector3f(0.1f, 0.1f, 0.1f));
        shader.setFloat("material.shininess", 16.0f);
        shader.setBool("useDirectionalLight", true);
        objectModel.draw(stack, shader, model);
    }

    public Vector3f getPosition() {
        return null;
    }
}
