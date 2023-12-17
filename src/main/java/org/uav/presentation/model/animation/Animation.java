package org.uav.presentation.model.animation;

import org.javatuples.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.uav.utils.PentaFunction;
import org.uav.utils.SlerpQuaternionInterpolator;

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

    public Vector3f getTranslationFrame(float progress) {
        return getFrame(translationAnimation, this::calcLinearInterpolation, progress);
    }

    public Quaternionf getRotationFrame(float progress) {
        return getFrame(rotationAnimation, SlerpQuaternionInterpolator::interpolate, progress);
    }

    public Vector3f getScaleFrame(float progress) {
        return getFrame(scaleAnimation, this::calcLinearInterpolation, progress);
    }

    public <T> T getFrame(List<Pair<Float, T>> animation, PentaFunction<Float, Float, Float, T, T, T> interpolator, float progress) {
        if(animation.isEmpty()) return null;
        float timeSpan = animation.get(animation.size()-1).getValue0() - animation.get(0).getValue0();
        float time = progress * timeSpan;

        int currentFrame = findFrame(time, animation);
        if(currentFrame >= 0) return animation.get(currentFrame).getValue1(); // Found the exact frame
        currentFrame = Math.abs(currentFrame + 1);
        if(currentFrame == 0) return animation.get(0).getValue1();
        var p1 = animation.get(currentFrame - 1);
        var p2 = animation.get(currentFrame);
        return interpolator.apply(time, p1.getValue0(), p2.getValue0(), p1.getValue1(), p2.getValue1());
    }

    private Vector3f calcLinearInterpolation(float point, float lowerBound, float upperBound, Vector3f lowerValue, Vector3f upperValue) {
        float ratio = (point - lowerBound) / (upperBound - lowerBound);
        return new Vector3f(
                (upperValue.x - lowerValue.x) * ratio + lowerValue.x,
                (upperValue.y - lowerValue.y) * ratio + lowerValue.y,
                (upperValue.z - lowerValue.z) * ratio + lowerValue.z
        );
    }

    private static <T> int findFrame(float time, List<Pair<Float, T>> animation) {
        return Collections.binarySearch(animation, new Pair<>(time, null), Comparator.comparing(Pair::getValue0));
    }

    public void startAnimation(float time, boolean loop) {
        startTime = time;
        looping = loop;
    }

}
