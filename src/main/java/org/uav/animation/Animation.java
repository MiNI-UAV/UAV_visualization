package org.uav.animation;

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
    private float localTime;
    private float lastGlobalTime;

    public Animation(
            List<Pair<Float, Vector3f>> translationAnimation,
            List<Pair<Float, Quaternionf>> rotationAnimation,
            List<Pair<Float, Vector3f>> scaleAnimation
    ) {
        this.translationAnimation = translationAnimation;
        this.rotationAnimation = rotationAnimation;
        this.scaleAnimation = scaleAnimation;
        startTime = 0;
        localTime = 0;
        lastGlobalTime = 0;
        looping = false;
    }

    public void updateLocalTime(float globalTime, float timeMultiplier) {
        float globalTimeDiff = globalTime - lastGlobalTime;
        lastGlobalTime = globalTime;
        localTime += globalTimeDiff * timeMultiplier;
    }

    public Vector3f getTranslationFrame() {
        return getFrame(translationAnimation, this::calcLinearInterpolation);
    }

    public Quaternionf getRotationFrame() {
        return getFrame(rotationAnimation, SlerpQuaternionInterpolator::interpolate);
    }

    public Vector3f getScaleFrame() {
        return getFrame(scaleAnimation, this::calcLinearInterpolation);
    }

    public <T> T getFrame(List<Pair<Float, T>> animation, PentaFunction<Float, Float, Float, T, T, T> interpolator) {
        if(animation.isEmpty()) return null;

        float time = localTime - startTime;
        if(time < animation.get(0).getValue0()) {
            if(looping)
            {
                float timeDiff = animation.get(animation.size()-1).getValue0() - animation.get(0).getValue0();
                while(localTime - startTime < animation.get(0).getValue0()) startTime -= timeDiff;
            } else return animation.get(0).getValue1(); // If out of bounds, get boundaries.
        } else if(time > animation.get(animation.size()-1).getValue0()) {
            if(looping) {
                float timeDiff = animation.get(animation.size()-1).getValue0() - animation.get(0).getValue0();
                while(localTime - startTime > animation.get(animation.size()-1).getValue0()) startTime += timeDiff;
            } else return animation.get(animation.size()-1).getValue1(); // As above
            time = localTime - startTime;
        }

        int currentFrame = findFrame(time, animation);
        if(currentFrame >= 0) return animation.get(currentFrame).getValue1(); // Found the exact frame
        currentFrame = Math.abs(currentFrame + 1);
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
