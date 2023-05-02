package org.opengl.model;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;

import java.util.List;
@Deprecated
public class ModelOld {

    public final List<Mesh> meshes;

    public ModelOld(List<Mesh> meshes) {
        this.meshes = meshes;
    }

    public void draw(MemoryStack stack, Shader shader, Matrix4f modelMatrix) {
        meshes.forEach(m -> m.draw(stack, shader, new Matrix4f()));
    }
}
