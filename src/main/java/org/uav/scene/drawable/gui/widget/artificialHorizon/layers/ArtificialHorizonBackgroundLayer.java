package org.uav.scene.drawable.gui.widget.artificialHorizon.layers;

import org.joml.Quaternionf;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.DrawableGuiLayer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;

public class ArtificialHorizonBackgroundLayer implements DrawableGuiLayer {
    private final static AffineTransform identity = new AffineTransform();
    private final BufferedImage horizonTexture;
    private final int distanceToMax;
    private final int horizonScreenX;
    private final int horizonScreenY;
    private float rotX;
    private float rotY;

    public ArtificialHorizonBackgroundLayer(
            BufferedImage horizonTexture,
            int distanceToMax,
            int horizonScreenX,
            int horizonScreenY
    ) {
        this.horizonTexture = horizonTexture;
        this.distanceToMax = distanceToMax;
        this.horizonScreenX = horizonScreenX;
        this.horizonScreenY = horizonScreenY;
        rotX = 0;
        rotY = 0;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().id);
        if(drone == null) return;
        Quaternionf q = drone.rotation;
        rotX = -((float) atan2(2 * (q.w * q.x + q.y * q.z), 1 - 2 * (q.x*q.x + q.y*q.y)));
        float droneRotY = (float) asin(2 * (q.w * q.y - q.x * q.z));
        rotY = droneRotY / (0.5f * (float) Math.PI) * distanceToMax;
    }
    @Override
    public void draw(Graphics2D g) {
        AffineTransform trans = new AffineTransform();
        trans.setTransform(identity);
        trans.translate(0, rotY);
        trans.translate((horizonScreenX - horizonTexture.getWidth()) / 2.f, (horizonScreenY - horizonTexture.getHeight() ) / 2.f);
        trans.rotate(rotX, horizonTexture.getWidth() / 2.f, horizonTexture.getHeight() / 2.f - rotY);
        g.drawImage(horizonTexture, trans, null);
    }
}
