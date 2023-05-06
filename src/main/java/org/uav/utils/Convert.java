package org.uav.utils;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.joml.Math.cos;
import static org.joml.Math.sin;

public class Convert {
    public static Quaternionf toQuaterion(Vector3f rotation) {
        var roll = rotation.x;
        var pitch = -rotation.z;
        var yaw = rotation.y;
        double cr = cos(roll * 0.5);
        double sr = sin(roll * 0.5);
        double cp = cos(pitch * 0.5);
        double sp = sin(pitch * 0.5);
        double cy = cos(yaw * 0.5);
        double sy = sin(yaw * 0.5);

        var x = sr * cp * cy - cr * sp * sy;
        var y = cr * sp * cy + sr * cp * sy;
        var z = cr * cp * sy - sr * sp * cy;
        var w = cr * cp * cy + sr * sp * sy;

        return new Quaternionf(x, y, z, w).normalize();
    }
}
