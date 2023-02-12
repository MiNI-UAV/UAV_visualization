package org.opengl.drawable;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.shader.Shader;

public interface Drawable {
    
    void draw(MemoryStack stack, Shader shader);
    Vector3f getPosition();
}
