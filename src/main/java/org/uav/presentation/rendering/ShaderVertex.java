package org.uav.presentation.rendering;

import java.nio.FloatBuffer;

public abstract class ShaderVertex {

    public abstract void insertIntoFloatBuffer(FloatBuffer fb);

    public abstract int getNumberOfFloats();
}
