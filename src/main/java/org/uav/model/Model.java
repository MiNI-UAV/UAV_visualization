package org.uav.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.scene.shader.Shader;

import java.util.List;
import java.util.function.Supplier;

public class Model {

    public final ModelNode rootNode;

    private Vector3f position;
    private Quaternionf rotation;

    public Model(ModelNode rootNode) {
        position = new Vector3f();
        rotation = new Quaternionf();
        this.rootNode = rootNode;
        this.rootNode.setCustomTranslationSupplier(()->position);
        this.rootNode.setCustomRotationSupplier(()-> rotation);
    }

    public void draw(MemoryStack stack, Shader shader) {
        rootNode.draw(stack, shader, new Matrix4f());
    }

    public void setAnimation(
            Supplier<Vector3f> translation,
            Supplier<Quaternionf> rotation,
            Supplier<Vector3f> scale,
            List<String> targetNodeName
    ) {
        rootNode.setAnimation(translation, rotation, scale, targetNodeName);
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }
    public Quaternionf getRotation() {
        return rotation;
    }
}
