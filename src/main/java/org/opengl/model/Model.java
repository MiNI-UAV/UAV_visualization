package org.opengl.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;

import java.util.List;
import java.util.function.Supplier;

import static org.opengl.utils.Convert.toQuaterion;

public class Model {

    public final ModelNode rootNode;

    private Vector3f position;
    private Vector3f rotation;

    public Model(ModelNode rootNode) {
        position = new Vector3f();
        rotation = new Vector3f();
        this.rootNode = rootNode;
        this.rootNode.setCustomTranslationSupplier(()->position);
        this.rootNode.setCustomRotationSupplier(()->toQuaterion(rotation));
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

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }
    public Vector3f getRotation() {
        return rotation;
    }
}
