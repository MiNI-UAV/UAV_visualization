package org.uav.model;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.scene.shader.Shader;

public class Model {

    private final ModelNode rootNode;

    @Getter @Setter
    private Vector3f position;
    @Getter @Setter
    private Quaternionf rotation;

    public Model(ModelNode rootNode) {
        position = new Vector3f();
        rotation = new Quaternionf();
        this.rootNode = rootNode;
    }

    public void draw(MemoryStack stack, Shader shader) {
        Matrix4f modelTransformation = new Matrix4f()
                .translate(position)
                .rotate(rotation)
                .scale(new Vector3f(1));
        rootNode.draw(stack, shader, modelTransformation);
    }
}
