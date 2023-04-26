package org.opengl.model;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;

import static org.joml.Math.cos;
import static org.joml.Math.sin;

public class Model {

    public final ModelNode rootNode;
    public Vector3f position = new Vector3f();
    public Vector3f rotation = new Vector3f(); // Immutability principle - setters and getters

    public Model(ModelNode rootNode) {
        this.rootNode = rootNode;
    }

    public void draw(MemoryStack stack, Shader shader) {
        rootNode.customTranslation = position;
        rootNode.customRotation = toQuaterion(rotation);
        rootNode.customScale = new Vector3f(0.1f);
        rootNode.draw(stack, shader, new Matrix4f());
    }

    private static Quaternionf toQuaterion(Vector3f rotation) {
        var roll = rotation.z;
        var pitch = rotation.y;
        var yaw = rotation.x;
        double cr = cos(roll * 0.5);
        double sr = sin(roll * 0.5);
        double cp = cos(pitch * 0.5);
        double sp = sin(pitch * 0.5);
        double cy = cos(yaw * 0.5);
        double sy = sin(yaw * 0.5);

        var w = cr * cp * cy + sr * sp * sy;
        var x = sr * cp * cy - cr * sp * sy;
        var y = cr * sp * cy + sr * cp * sy;
        var z = cr * cp * sy - sr * sp * cy;

        return new Quaternionf(x, y, z, w).normalize();
    }

    public Vector3f getPosition() {
    return new Vector3f(position);
    };
}
