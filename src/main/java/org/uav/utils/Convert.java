package org.uav.utils;

import org.joml.Quaternionf;
import org.joml.Vector3f;

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
}
