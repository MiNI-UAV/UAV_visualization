package org.uav.presentation.model.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class AnimationPlayer {
    private final Map<String, Animation> animations;

    public AnimationPlayer() {
        animations = new HashMap<>();
    }

    public void put(String name, Animation animation) {
        animations.put(name, animation);
    }


    public Vector3f getTranslationOrDefault(Vector3f defaultTranslation, Float progress) { // TODO Zwinąć
        if(progress == null) return defaultTranslation;
        var translation = new Vector3f();
        animations.values().forEach(animation -> {
            var frame = animation.getTranslationFrame(progress);
            if(frame == null) return;
            translation.add(frame);
        });
        if(translation.equals(new Vector3f())) return defaultTranslation;
        return translation;
    }

    public Quaternionf getRotationOrDefault(Quaternionf defaultRotation, Float progress) {
        if(progress == null) return defaultRotation;
        var rotation = new Quaternionf();
        animations.values().forEach(animation -> {
            var frame = animation.getRotationFrame(progress);
            if(frame == null) return;
            rotation.mul(frame);
        });
        if(rotation.equals(new Quaternionf())) return defaultRotation;
        return rotation;
    }

    public Vector3f getScaleOrDefault(Vector3f defaultScale, Float progress) {
        if(progress == null) return defaultScale;
        var scale = new Vector3f(1);
        animations.values().forEach(animation -> {
            var frame = animation.getScaleFrame(progress);
            if(frame == null) return;
            scale.mul(frame);
        });
        if(scale.equals(new Vector3f(1))) return defaultScale;
        return scale;
    }
}
