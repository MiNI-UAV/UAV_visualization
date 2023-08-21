package org.uav.scene.drawable.gui.widget.artificialHorizon.layers;

import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.DrawableGuiLayer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

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
        rotX = -drone.rotation.x;
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
