package org.uav.utils;

import org.joml.Vector3f;
import org.uav.presentation.model.Texture;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocateDirect;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL21.GL_SRGB;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.uav.utils.IOUtils.extractImageData;

public class OpenGLUtils {

    public static final int OPENGL_CANVAS_SIZE = 2;
    public static final int OPENGL_CANVAS_LEFT = -1;
    public static final int OPENGL_CANVAS_RIGHT = 1;
    public static final int OPENGL_CANVAS_BOTTOM = -1;
    public static final int OPENGL_CANVAS_TOP = 1;

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

    public static Texture setupTexture(BufferedImage img) {
        var imageDirectByteBuffer = allocateDirect(img.getHeight() * img.getWidth() * img.getColorModel().getNumComponents());
        imageDirectByteBuffer.put(ByteBuffer.wrap(extractImageData(img)));
        imageDirectByteBuffer.position(0);

        int components = img.getColorModel().getNumComponents();
        int format = switch(components) {
            case 1 -> GL_BACK;
            case 4 -> GL_RGBA;
            default -> GL_RGB;
        };
        ColorSpace colorSpace = img.getColorModel().getColorSpace();
        int internalFormat = switch(components) {
            case 1 -> GL_BACK;
            case 4 -> colorSpace.isCS_sRGB()? GL_SRGB_ALPHA: GL_RGBA;
            default -> colorSpace.isCS_sRGB()? GL_SRGB: GL_RGB;
        };


        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, img.getWidth(), img.getHeight(), 0, format, GL_UNSIGNED_BYTE, imageDirectByteBuffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        return new Texture(textureId, "texture_diffuse", false);
    }
}
