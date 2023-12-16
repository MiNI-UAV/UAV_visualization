package org.uav.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.animation.AnimationPlayer;
import org.uav.scene.OrderedRenderQueue;
import org.uav.scene.shader.Shader;

import java.util.List;
import java.util.Map;

public class ModelNode {

    private final String name;
    private final List<Mesh> meshes;
    private final List<ModelNode> children;
    private final Vector3f localTranslation;
    private final Quaternionf localRotation;
    private final Vector3f localScale;
    private final AnimationPlayer animationPlayer;

    public ModelNode(
            String name,
            List<Mesh> meshes,
            List<ModelNode> children,
            Vector3f localTranslation,
            Quaternionf localRotation,
            Vector3f localScale,
            AnimationPlayer animationPlayer) {
        this.name = name;
        this.meshes = meshes;
        this.children = children;
        this.localTranslation = localTranslation;
        this.localRotation = localRotation;
        this.localScale = localScale;
        this.animationPlayer = animationPlayer;
    }

    public void draw(MemoryStack stack, Shader shader, Matrix4f parentTransform, Map<String, Map<String, Float>> animationProgressPerModelNode) {
        Matrix4f globalTransformation = getGlobalTransformation(parentTransform, animationProgressPerModelNode);
        meshes.forEach(m -> m.draw(stack, shader, globalTransformation));
        children.forEach(n -> n.draw(stack, shader, globalTransformation, animationProgressPerModelNode));
    }

    public void addToQueue(OrderedRenderQueue orderedRenderQueue, Matrix4f parentTransform, Shader shader, Map<String, Map<String, Float>> animationProgressPerModelNode) {
        Matrix4f globalTransformation = getGlobalTransformation(parentTransform, animationProgressPerModelNode);
        meshes.forEach(m -> orderedRenderQueue.addMesh(m, globalTransformation, shader));
        children.forEach(n -> n.addToQueue(orderedRenderQueue, globalTransformation, shader, animationProgressPerModelNode));
    }

    private Matrix4f getGlobalTransformation(Matrix4f parentTransform, Map<String, Map<String, Float>> animationProgressPerModelNode) {
        Matrix4f localTransformation = new Matrix4f()
                .translate(animationPlayer.getTranslationOrDefault(localTranslation, animationProgressPerModelNode.get("translation").get(name)))
                .rotate(animationPlayer.getRotationOrDefault(localRotation, animationProgressPerModelNode.get("rotation").get(name)))
                .scale(animationPlayer.getScaleOrDefault(localScale, animationProgressPerModelNode.get("scale").get(name)));
        return new Matrix4f(parentTransform).mul(localTransformation);
    }
}
