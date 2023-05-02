package org.uav.model;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.shader.Shader;

import java.util.List;
import java.util.function.Supplier;

public class ModelNode {

    private final String name;
    private final List<Mesh> meshes;
    private final List<ModelNode> children;

    private Vector3f localTranslation;
    private Quaternionf localRotation;
    private Vector3f localScale;
    private Supplier<Vector3f> customTranslationSupplier;
    private Supplier<Quaternionf> customRotationSupplier;
    private Supplier<Vector3f> customScaleSupplier;

    public ModelNode(String name, List<Mesh> meshes, List<ModelNode> children, Vector3f localTranslation, Quaternionf localRotation, Vector3f localScale) {
        this.name = name;
        this.meshes = meshes;
        this.children = children;
        this.localTranslation = localTranslation;
        this.localRotation = localRotation;
        this.localScale = localScale;
        this.customTranslationSupplier = Vector3f::new;
        this.customRotationSupplier = Quaternionf::new;
        this.customScaleSupplier = () -> new Vector3f(1);
    }

    public void draw(MemoryStack stack, Shader shader, Matrix4f parentTransform) {
        Vector3f translation = new Vector3f().add(localTranslation).add(customTranslationSupplier.get());
        Quaternionf rotation = new Quaternionf().mul(localRotation).mul(customRotationSupplier.get());
        var customScale = customScaleSupplier.get();
        Vector3f scale = new Vector3f(localScale.x * customScale.x, localScale.y * customScale.y, localScale.z * customScale.z);

        Matrix4f localTransformation = new Matrix4f()
                .translate(translation)
                .rotate(rotation)
                .scale(scale);
        Matrix4f globalTransformation = new Matrix4f(parentTransform).mul(localTransformation);

        meshes.forEach(m -> m.draw(stack, shader, globalTransformation));
        children.forEach(n -> n.draw(stack, shader, globalTransformation));
    }

    public void setCustomTranslationSupplier(Supplier<Vector3f> customTranslationSupplier) {
        this.customTranslationSupplier = customTranslationSupplier;
    }

    public void setCustomRotationSupplier(Supplier<Quaternionf> customRotationSupplier) {
        this.customRotationSupplier = customRotationSupplier;
    }

    public void setAnimation(
            Supplier<Vector3f> translation,
            Supplier<Quaternionf> rotation,
            Supplier<Vector3f> scale,
            List<String> targetNodeName
    ) {
        if(targetNodeName.stream().anyMatch(n -> n.equals(name))){
            if(translation != null) customTranslationSupplier = translation;
            if(rotation != null) customRotationSupplier = rotation;
            if(scale != null) customScaleSupplier = scale;
        }
        else for (ModelNode child : children) {
            child.setAnimation(translation, rotation, scale, targetNodeName);
        }
    }
}
