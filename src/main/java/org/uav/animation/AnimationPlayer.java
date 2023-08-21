package org.uav.animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationPlayer {
    private List<String> activeAnimations;
    private Map<String, Animation> animations;

    public AnimationPlayer() {
        activeAnimations = new ArrayList<>();
        animations = new HashMap<>();
    }

    public void put(String name, Animation animation) {
        animations.put(name, animation);
    }

    public void start(String name, float time, boolean loop) {
        if(!animations.containsKey(name)) return;
        animations.get(name).startAnimation(time, loop);
        activeAnimations.add(name);
    }

    public Vector3f getTranslationOrDefault(Vector3f defaultTranslation, float time) { // TODO Zwinąć
        if(activeAnimations.isEmpty()) return defaultTranslation;
        var translation = new Vector3f();
        activeAnimations.forEach(activeAnimation -> {
            Animation animation = animations.getOrDefault(activeAnimation, null);
            if(animation == null) return;
            var frame = animation.getTranslationFrame(time);
            if(frame == null) return;
            translation.add(frame);
        });
        if(translation.equals(new Vector3f())) return defaultTranslation;
        return translation;
    }

    public Quaternionf getRotationOrDefault(Quaternionf defaultRotation, float time) {
        if(activeAnimations.isEmpty()) return defaultRotation;
        var rotation = new Quaternionf();
        activeAnimations.forEach(activeAnimation -> {
            Animation animation = animations.getOrDefault(activeAnimation, null);
            if(animation == null) return;
            var frame = animation.getRotationFrame(time);
            if(frame == null) return;
            rotation.mul(frame);
        });
        if(rotation.equals(new Quaternionf())) return defaultRotation;
        return rotation;
    }

    public Vector3f getScaleOrDefault(Vector3f defaultScale, float time) {
        if(activeAnimations.isEmpty()) return defaultScale;
        var scale = new Vector3f(1);
        activeAnimations.forEach(activeAnimation -> {
            Animation animation = animations.getOrDefault(activeAnimation, null);
            if(animation == null) return;
            var frame = animation.getScaleFrame(time);
            if(frame == null) return;
            scale.mul(frame);
        });
        if(scale.equals(new Vector3f(1))) return defaultScale;
        return scale;
    }
}
