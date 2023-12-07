package org.uav.model;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.scene.OrderedRenderQueue;
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
        var modelTransformation = getModelTransformation();
        if(modelTransformation == null) return;
        rootNode.draw(stack, shader, modelTransformation, currentTime);
    }

    public void addToQueue(OrderedRenderQueue orderedRenderQueue, Shader shader, float currentTime) {
        var modelTransformation = getModelTransformation();
        if(modelTransformation == null) return;
        rootNode.addToQueue(orderedRenderQueue, modelTransformation, shader, currentTime);
    }

    public Matrix4f getModelTransformation() {
        return position == null || rotation == null ? null :
            new Matrix4f()
                .translate(position)
                .rotate(rotation)
                .scale(scale);
    }
}
