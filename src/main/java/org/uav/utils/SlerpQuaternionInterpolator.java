package org.uav.utils;

import org.joml.Quaternionf;

// https://github.com/javagl/JglTF/blob/master/jgltf-model/src/main/java/de/javagl/jgltf/model/animation/SlerpQuaternionInterpolator.java
public class SlerpQuaternionInterpolator {
    public static Quaternionf interpolate( float alpha, float bound1, float bound2, Quaternionf a, Quaternionf b)
    {
        // Adapted from javax.vecmath.Quat4f
        float ratio = (alpha - bound1) / (bound2 - bound1);

        float dot = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
        if (dot < 0)
        {
            b.x = -b.x;
            b.y = -b.y;
            b.z = -b.z;
            b.w = -b.w;
            dot = -dot;
        }
        float epsilon = 1e-6f;
        float s0, s1;
        if ((1.0 - dot) > epsilon)
        {
            float omega = (float)Math.acos(dot);
            float invSinOmega = 1.0f / (float)Math.sin(omega);
            s0 = (float)Math.sin((1.0 - ratio) * omega) * invSinOmega;
            s1 = (float)Math.sin(ratio * omega) * invSinOmega;
        }
        else
        {
            s0 = 1.0f - ratio;
            s1 = ratio;
        }
        float rx = s0 * a.x + s1 * b.x;
        float ry = s0 * a.y + s1 * b.y;
        float rz = s0 * a.z + s1 * b.z;
        float rw = s0 * a.w + s1 * b.w;
        return new Quaternionf(rx, ry, rz, rw);
    }
}
