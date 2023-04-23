package org.opengl.drawable;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.ModelOld;
import org.opengl.shader.Shader;

import java.util.List;

public class DrawableDrone extends Drawable {

    private final ModelOld objectModel;

    private final List<DrawablePropellers> childrenDrawables;

    public Vector3f position;
    public Vector3f rotation;

    public DrawableDrone(ModelOld objectModel, List<DrawablePropellers> childrenDrawables) {
        this.objectModel = objectModel;
        this.childrenDrawables = childrenDrawables;
        position = new Vector3f(-1f,-1f,-3f);
        rotation = new Vector3f();
    }

    @Override
    public void draw(MemoryStack stack, Shader shader) {
        shader.use();

        var model = new Matrix4f()
                .translate(getPosition())
                .rotate(rotation.x, new Vector3f(1f, 0f, 0f))
                .rotate(rotation.z, new Vector3f(0f, 1f, 0f))
                .rotate(rotation.y, new Vector3f(0f, 0f, 1f))
                .scale(0.1f);


        shader.setMatrix4f(stack,"model", model);
        shader.setVec3("material.ambient", new Vector3f(0.1f, 0.1f, 0.1f));
        shader.setVec3("material.diffuse", new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setVec3("material.specular", new Vector3f(0.1f, 0.1f, 0.1f));
        shader.setFloat("material.shininess", 16.0f);
        objectModel.draw(stack, shader, model);
        childrenDrawables.stream().forEach(propeller -> propeller.draw(getPosition(), getRotation(), new Vector3f(0.01f) , stack, shader));
    }

    @Override
    public void draw(Vector3f parentTranslation, Vector3f parentScaling, MemoryStack stack, Shader shader) {

    }

    @Override
    public Vector3f getPosition()  {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Matrix4f getRotationMatrix() {
        var x = rotation.x;
        var rx = new Matrix3f(
            1, 0, 0,
            0, (float)Math.cos(x), (float)Math.sin(x),
            0, (float)Math.sin(x), (float)Math.cos(x)
        );
        var y = rotation.y;
        var ry = new Matrix3f(
                (float)Math.cos(x), 0, (float)Math.sin(x),
                0, 1, (float)Math.sin(x),
                -(float)Math.sin(x), 0, (float)Math.cos(x)
        );
        var z = rotation.z;
        var rz = new Matrix3f(
                (float)Math.cos(x), -(float)Math.sin(x), 0,
                (float)Math.sin(x), (float)Math.cos(x), 0,
                0, 0, 1
        );
        var r = rz.mul(ry).mul(rx);
        return new Matrix4f(
                rx.m00, rx.m01, rx.m02, 0,
                rx.m10, rx.m11, rx.m12, 0,
                rx.m20, rx.m21, rx.m22, 0,
                0, 0, 0, 1
        );
    }

}