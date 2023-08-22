package org.uav.scene.drawable.gui.widget.artificialHorizon.layers;

import org.joml.Quaternionf;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.DrawableGuiLayer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static java.lang.Math.atan2;

public class ArtificialHorizonRollLayer implements DrawableGuiLayer {
    private final static AffineTransform identity = new AffineTransform();
    private final BufferedImage horizonRollTexture;
    private final int horizonScreenX;
    private final int horizonScreenY;
    private float rotX;

    public ArtificialHorizonRollLayer(
            BufferedImage horizonRollTexture,
            int horizonScreenX,
            int horizonScreenY
    ) {
        this.horizonRollTexture = horizonRollTexture;
        this.horizonScreenX = horizonScreenX;
        this.horizonScreenY = horizonScreenY;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().id);
        if(drone == null) return;
        Quaternionf q = drone.rotation;
        rotX = -((float) atan2(2 * (q.w * q.x + q.y * q.z), 1 - 2 * (q.x*q.x + q.y*q.y)));
    }
    @Override
    public void draw(Graphics2D g) {
        AffineTransform trans = new AffineTransform();
        trans.setTransform(identity);
        trans.translate((horizonScreenX - horizonRollTexture.getWidth()) / 2.f, (horizonScreenY - horizonRollTexture.getHeight() ) / 2.f);
        trans.rotate(rotX, horizonRollTexture.getWidth() / 2.f, horizonRollTexture.getHeight() / 2.f);
        g.drawImage(horizonRollTexture, trans, null);
    }
}
