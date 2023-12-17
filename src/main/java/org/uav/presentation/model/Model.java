package org.uav.presentation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.presentation.rendering.OrderedRenderQueue;
import org.uav.presentation.rendering.Shader;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {

    private final ModelNode rootNode;
    @Getter
    private final List<AnimationInfo> animationInfos;

    @Getter @Setter @Nullable
    private Vector3f position;
    @Getter @Setter @Nullable
    private Quaternionf rotation;
    @Getter @Setter @Nullable
    private Vector3f scale;

    public Model(ModelNode rootNode, List<AnimationInfo> animationInfos) {
        this.rootNode = rootNode;
        this.animationInfos = animationInfos;
        position = null;
        rotation = null;
        scale = new Vector3f(1);
    }

    public void draw(MemoryStack stack, Shader shader) {
        var modelTransformation = getModelTransformation();
        if(modelTransformation == null) return;
        var map = new HashMap<String, Map<String, Float>>();
        map.put("translation", new HashMap<>());
        map.put("rotation", new HashMap<>());
        map.put("scale", new HashMap<>());
        animationInfos.forEach(info -> map.get(info.animationType).put(info.animatedModelName, info.animationProgress));
        rootNode.draw(stack, shader, modelTransformation, map);
    }

    public void addToQueue(OrderedRenderQueue orderedRenderQueue, Shader shader) {
        var modelTransformation = getModelTransformation();
        if(modelTransformation == null) return;
        var map = new HashMap<String, Map<String, Float>>();
        map.put("translation", new HashMap<>());
        map.put("rotation", new HashMap<>());
        map.put("scale", new HashMap<>());
        animationInfos.forEach(info -> map.get(info.animationType).put(info.animatedModelName, info.animationProgress));
        rootNode.addToQueue(orderedRenderQueue, modelTransformation, shader, map);
    }

    public Matrix4f getModelTransformation() {
        return position == null || rotation == null ? null :
            new Matrix4f()
                .translate(position)
                .rotate(rotation)
                .scale(scale);
    }

    @AllArgsConstructor
    @Data
    public static class AnimationInfo {
        String animationName;
        String animationType;
        String animatedModelName;
        float animationTimeSpan;
        float animationProgress;
    }
}
