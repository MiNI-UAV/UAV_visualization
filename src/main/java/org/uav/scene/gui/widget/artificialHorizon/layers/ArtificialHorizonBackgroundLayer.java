package org.uav.scene.gui.widget.artificialHorizon.layers;

import org.uav.model.SimulationState;
import org.uav.model.controlMode.ControlModeReply;
import org.uav.scene.gui.DrawableGuiLayer;
import org.uav.utils.Convert;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ArtificialHorizonBackgroundLayer implements DrawableGuiLayer {
    private final static AffineTransform identity = new AffineTransform();
    private final BufferedImage horizonTexture;
    private final int distanceToMax;
    private final int horizonScreenX;
    private final int horizonScreenY;
    private float rotX;
    private float rotY;
    private float demandedRotY;
    private boolean drawDemandedRotY;

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
        drawDemandedRotY = false;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isEmpty()) return;
        var rotation = Convert.toEuler(drone.get().droneStatus.rotation);
        rotX = -rotation.x;
        rotY = rotation.y / (0.5f * (float) Math.PI) * distanceToMax;

        updateDemanded(simulationState);
    }

    private void updateDemanded(SimulationState simulationState) {
        if(
            simulationState.getCurrentControlModeDemanded() == null ||
            !simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.PITCH)
        )
            drawDemandedRotY = false;
        else {
            float demanded = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.PITCH);
            demandedRotY = rotY - (demanded / (0.5f * (float) Math.PI) * distanceToMax);
            drawDemandedRotY = true;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform trans = new AffineTransform();
        trans.setTransform(identity);
        trans.translate(0, rotY);
        trans.translate((horizonScreenX - horizonTexture.getWidth()) / 2.f, (horizonScreenY - horizonTexture.getHeight() ) / 2.f);
        trans.rotate(rotX, horizonTexture.getWidth() / 2.f, horizonTexture.getHeight() / 2.f - rotY);
        g.drawImage(horizonTexture, trans, null);

        drawDemanded(g);
    }

    private void drawDemanded(Graphics2D g) {
        if(drawDemandedRotY) {
            g.translate(0, demandedRotY);
            g.translate((horizonScreenX - horizonTexture.getWidth()) / 2.f, (horizonScreenY - horizonTexture.getHeight() ) / 2.f);
            g.rotate(rotX, horizonTexture.getWidth() / 2.f, horizonTexture.getHeight() / 2.f - demandedRotY);
            g.setColor(new Color(1,0,0,0.5f));
            g.setStroke(new BasicStroke(3));
            g.drawOval((int) (horizonTexture.getWidth() / 2.f )- 8, (int) (horizonTexture.getHeight() / 2.f) - 8, 16, 16);
            g.transform(identity);
            g.setStroke(new BasicStroke());
        }
    }
}
