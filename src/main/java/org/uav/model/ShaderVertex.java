package org.uav.model;

import java.nio.FloatBuffer;

public abstract class ShaderVertex {

    public abstract void insertIntoFloatBuffer(FloatBuffer fb);

    public abstract int getNumberOfFloats();
}
