package org.opengl.model;

import de.javagl.jgltf.model.NodeModel;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;

public class Model {

    public final ModelNode rootNode;

    public Model(ModelNode rootNode) {
        this.rootNode = rootNode;
    }

    public void draw(MemoryStack stack, Shader shader) {
        rootNode.draw(stack, shader, new Vector3f(), new Quaternionf(), new Vector3f(1f));
    }
}
