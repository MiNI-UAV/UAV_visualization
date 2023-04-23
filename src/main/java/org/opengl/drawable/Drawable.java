package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;

public abstract class Drawable {

    public abstract void draw(MemoryStack stack, Shader shader);
    public abstract void draw(Vector3f parentTranslation, Vector3f parentScaling, MemoryStack stack, Shader shader);
    public abstract Vector3f getPosition();
}
