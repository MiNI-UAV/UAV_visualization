package org.uav.utils;

import org.joml.Vector3f;

public class OpenGLUtils {

    public static Vector3f getSunDirectionVector(Vector3f startVector, float sunAngleYearCycle, float sunAngleDayCycle) {
        return startVector
                .rotateY((90 - sunAngleYearCycle) / 180 * (float) Math.PI)
                .rotateX(-sunAngleDayCycle / 180 * (float) Math.PI);
    }
}
