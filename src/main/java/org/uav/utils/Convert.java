package org.uav.utils;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static org.joml.Math.cos;
import static org.joml.Math.sin;

public class Convert {
    public static Quaternionf toQuaternion(Vector3f rotation) {
        var roll = rotation.x;
        var pitch = rotation.y;
        var yaw = rotation.z;

        var qx = new Quaternionf(sin(roll/2),0,0,cos(roll/2));
        var qy = new Quaternionf(0,sin(pitch/2),0,cos(pitch/2));
        var qz = new Quaternionf(0,0,sin(yaw/2),cos(yaw/2));
        return qz.mul(qy).mul(qx);
    }

    public static Vector3f toEuler(Quaternionf q) {
        float x = (float) atan2(2 * (q.w * q.x + q.y * q.z), 1 - 2 * (q.x*q.x + q.y*q.y));
        float y = (float) asin(2 * (q.w * q.y - q.x * q.z));
        float z = (float) atan2(2 * (q.w * q.z + q.x * q.y), 1 - 2 * (q.y*q.y + q.z*q.z));
        return new Vector3f(x, y, z);
    }
}
