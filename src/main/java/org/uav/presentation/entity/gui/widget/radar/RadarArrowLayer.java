package org.uav.presentation.entity.gui.widget.radar;

import org.joml.Matrix3x2f;
import org.lwjgl.system.MemoryStack;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;

public class RadarArrowLayer {
    private final Sprite radarArrowSprite;
    private float radarArrowAngle;

    public RadarArrowLayer(BufferedImage radarArrowTexture, Shader spriteShader) {
        radarArrowSprite = new Sprite(radarArrowTexture, spriteShader);
    }

    public void update(float radarArrowAngle) {
        this.radarArrowAngle = radarArrowAngle;
    }
    public void draw(MemoryStack stack) {
        var transform = new Matrix3x2f();
        transform.rotateAbout(-radarArrowAngle, 0.5f,0.5f);
        radarArrowSprite.setTransform(transform);
        radarArrowSprite.draw(stack);
    }
}
