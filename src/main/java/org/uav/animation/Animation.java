package org.uav.animation;

import org.javatuples.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Animation {

    private final List<Pair<Float, Vector3f>> translationAnimation;
    private final List<Pair<Float, Quaternionf>> rotationAnimation;
    private final List<Pair<Float, Vector3f>> scaleAnimation;
    private float startTime;
    private boolean looping;

    public Animation(
            List<Pair<Float, Vector3f>> translationAnimation,
            List<Pair<Float, Quaternionf>> rotationAnimation,
            List<Pair<Float, Vector3f>> scaleAnimation
    ) {
        this.translationAnimation = translationAnimation;
        this.rotationAnimation = rotationAnimation;
        this.scaleAnimation = scaleAnimation;
        startTime = 0;
        looping = false;
    }

    public Vector3f getTranslationFrame(float globalTime) { // TODO Duplicates
        if(translationAnimation.isEmpty()) return null; // TODO Or not looping and out of bounds

        float time = globalTime - startTime;
        if(time < translationAnimation.get(0).getValue0()) {
            if(looping)
            {
                float timeDiff = translationAnimation.get(translationAnimation.size()-1).getValue0() - translationAnimation.get(0).getValue0();
                while(time < translationAnimation.get(0).getValue0()) time += timeDiff;
            } else return translationAnimation.get(0).getValue1(); // If out of bounds, get boundaries.
        } else if(time > translationAnimation.get(translationAnimation.size()-1).getValue0()) {
            if(looping) {
                float timeDiff = translationAnimation.get(translationAnimation.size()-1).getValue0() - translationAnimation.get(0).getValue0();
                while(time > translationAnimation.get(translationAnimation.size()-1).getValue0()) time -= timeDiff;
            } else return translationAnimation.get(translationAnimation.size()-1).getValue1(); // As above
        }

        int currentFrame = findFrame(time, translationAnimation);
        if(currentFrame >= 0) return translationAnimation.get(currentFrame).getValue1(); // Found the exact frame
        currentFrame = Math.abs(currentFrame + 1);
        var p1 = translationAnimation.get(currentFrame - 1);
        var p2 = translationAnimation.get(currentFrame);
        return calcLinearInterpolation(time, p1.getValue0(), p2.getValue0(), p1.getValue1(), p2.getValue1());
    }

    public Quaternionf getRotationFrame(float globalTime) {
        if(rotationAnimation.isEmpty()) return null;

        float time = globalTime - startTime;
        if(time < rotationAnimation.get(0).getValue0()) {
            if(looping)
            {
                float timeDiff = rotationAnimation.get(rotationAnimation.size()-1).getValue0() - rotationAnimation.get(0).getValue0();
                while(time < rotationAnimation.get(0).getValue0()) time += timeDiff;
            } else return rotationAnimation.get(0).getValue1(); // If out of bounds, get boundaries.
        } else if(time > rotationAnimation.get(rotationAnimation.size()-1).getValue0()) {
            if(looping) {
                float timeDiff = rotationAnimation.get(rotationAnimation.size()-1).getValue0() - rotationAnimation.get(0).getValue0();
                while(time > rotationAnimation.get(rotationAnimation.size()-1).getValue0()) time -= timeDiff;
            } else return rotationAnimation.get(rotationAnimation.size()-1).getValue1(); // As above
        }

        int currentFrame = findFrame(time, rotationAnimation);
        if(currentFrame >= 0) return rotationAnimation.get(currentFrame).getValue1(); // Found the exact frame
        currentFrame = Math.abs(currentFrame + 1);
        var p1 = rotationAnimation.get(currentFrame - 1);
        var p2 = rotationAnimation.get(currentFrame);
        return p1.getValue1(); // TODO quaternion interpolation
    }

    public Vector3f getScaleFrame(float globalTime) {
        if(scaleAnimation.isEmpty()) return null;

        float time = globalTime - startTime;
        if(time < scaleAnimation.get(0).getValue0()) {
            if(looping)
            {
                float timeDiff = scaleAnimation.get(scaleAnimation.size()-1).getValue0() - scaleAnimation.get(0).getValue0();
                while(time < scaleAnimation.get(0).getValue0()) time += timeDiff;
            } else return scaleAnimation.get(0).getValue1(); // If out of bounds, get boundaries.
        } else if(time > scaleAnimation.get(scaleAnimation.size()-1).getValue0()) {
            if(looping) {
                float timeDiff = scaleAnimation.get(scaleAnimation.size()-1).getValue0() - scaleAnimation.get(0).getValue0();
                while(time > scaleAnimation.get(scaleAnimation.size()-1).getValue0()) time -= timeDiff;
            } else return scaleAnimation.get(scaleAnimation.size()-1).getValue1(); // As above
        }
        int currentFrame = findFrame(time, scaleAnimation);
        if(currentFrame >= 0) return scaleAnimation.get(currentFrame).getValue1(); // Found the exact frame
        currentFrame = Math.abs(currentFrame + 1);
        var p1 = scaleAnimation.get(currentFrame - 1);
        var p2 = scaleAnimation.get(currentFrame);
        return calcLinearInterpolation(time, p1.getValue0(), p2.getValue0(), p1.getValue1(), p2.getValue1());
    }

    private Vector3f calcLinearInterpolation(float point, float lowerBound, float upperBound, Vector3f lowerValue, Vector3f upperValue) {
        float ratio = (point - lowerBound) / (upperBound - lowerBound); // TODO Better maths maybe
        return new Vector3f(
                (upperValue.x - lowerValue.x) * ratio + lowerValue.x,
                (upperValue.y - lowerValue.y) * ratio + lowerValue.y,
                (upperValue.z - lowerValue.z) * ratio + lowerValue.z
        );
    }

    private static <T> int findFrame(float time, List<Pair<Float, T>> animation) { // TODO: Check for not found
        return Collections.binarySearch(animation, new Pair<>(time, null), Comparator.comparing(Pair::getValue0));
    }

    public void startAnimation(float time, boolean loop) {
        startTime = time;
        looping = loop;
    }

}
