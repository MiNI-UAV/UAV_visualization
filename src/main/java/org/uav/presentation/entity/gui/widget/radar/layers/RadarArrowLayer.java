package org.uav.presentation.entity.gui.widget.radar.layers;

import org.uav.presentation.entity.gui.DrawableGuiLayer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class RadarArrowLayer implements DrawableGuiLayer {
    private final BufferedImage radarArrowTexture;
    private float radarArrowAngle;

    public RadarArrowLayer(
            BufferedImage radarArrowTexture
    ) {
        this.radarArrowTexture = radarArrowTexture;
    }

    public void update(float radarArrowAngle) {
        this.radarArrowAngle = radarArrowAngle;
    }
    @Override
    public void draw(Graphics2D g) {
        AffineTransform identity = new AffineTransform();
        AffineTransform trans = new AffineTransform();
        trans.setTransform(identity);
        trans.rotate(radarArrowAngle, (float) radarArrowTexture.getWidth() / 2, (float) radarArrowTexture.getHeight() / 2);
        g.drawImage(radarArrowTexture, trans, null);
    }
}
