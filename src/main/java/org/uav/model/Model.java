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
    @Getter @Setter
    private Vector3f scale;

    public Model(ModelNode rootNode) {
        position = null;
        rotation = null;
        scale = new Vector3f(1);
        this.rootNode = rootNode;
    }

    public void draw(MemoryStack stack, Shader shader, float currentTime) {
        if(position == null || rotation == null) return;
        Matrix4f modelTransformation = new Matrix4f()
                .translate(position)
                .rotate(rotation)
                .scale(scale);
        rootNode.draw(stack, shader, modelTransformation, currentTime);
    }
}
