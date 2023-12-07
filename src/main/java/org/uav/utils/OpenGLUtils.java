package org.uav.utils;

import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11C.*;

public class OpenGLUtils {

    public static void drawWithDepthFunc(Runnable drawingFunc, int depthMode) {
        int previousDepthFunc = glGetInteger(GL_DEPTH_FUNC);
        glDepthFunc(depthMode);
        drawingFunc.run();
        glDepthFunc(previousDepthFunc);
    }

    public static Vector3f getSunDirectionVector(Vector3f startVector, float sunAngleYearCycle, float sunAngleDayCycle) {
        return startVector
                .rotateY((90 - sunAngleYearCycle) / 180 * (float) Math.PI)
                .rotateX(-sunAngleDayCycle / 180 * (float) Math.PI);
    }
}
