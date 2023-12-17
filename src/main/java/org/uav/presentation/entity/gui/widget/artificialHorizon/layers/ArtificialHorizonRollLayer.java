package org.uav.presentation.entity.gui.widget.artificialHorizon.layers;

import org.uav.logic.state.controlMode.ControlModeReply;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.DrawableGuiLayer;
import org.uav.utils.Convert;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ArtificialHorizonRollLayer implements DrawableGuiLayer {
    private final static AffineTransform identity = new AffineTransform();
    private final BufferedImage horizonRollTexture;
    private final int horizonScreenX;
    private final int horizonScreenY;
    private float rotX;
    private float demandedRotX;
    private boolean drawDemandedRotX;

    public ArtificialHorizonRollLayer(
            BufferedImage horizonRollTexture,
            int horizonScreenX,
            int horizonScreenY
    ) {
        this.horizonRollTexture = horizonRollTexture;
        this.horizonScreenX = horizonScreenX;
        this.horizonScreenY = horizonScreenY;
        drawDemandedRotX = false;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isEmpty()) return;
        var rotation = Convert.toEuler(drone.get().droneStatus.rotation);
        rotX = -rotation.x;
        updateDemanded(simulationState);
    }

    private void updateDemanded(SimulationState simulationState) {
        if(
            simulationState.getCurrentControlModeDemanded() == null ||
            !simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.ROLL)
        )
            drawDemandedRotX = false;
        else {
            float demanded = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.ROLL);
            demandedRotX = rotX - (-demanded);
            drawDemandedRotX = true;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform trans = new AffineTransform();
        trans.setTransform(identity);
        trans.translate((horizonScreenX - horizonRollTexture.getWidth()) / 2.f, (horizonScreenY - horizonRollTexture.getHeight() ) / 2.f);
        trans.rotate(rotX, horizonRollTexture.getWidth() / 2.f, horizonRollTexture.getHeight() / 2.f);
        g.drawImage(horizonRollTexture, trans, null);

        drawDemanded(g);
    }

    private void drawDemanded(Graphics2D g) {
        AffineTransform trans;
        if(drawDemandedRotX) {
            trans = new AffineTransform();
            trans.setTransform(identity);
            g.translate((horizonScreenX - horizonRollTexture.getWidth()) / 2.f, (horizonScreenY - horizonRollTexture.getHeight() ) / 2.f);
            g.rotate(demandedRotX, horizonRollTexture.getWidth() / 2.f, horizonRollTexture.getHeight() / 2.f);
            g.setStroke(new BasicStroke(5));
            g.setColor(new Color(1,0,0,0.5f));
            g.drawLine(
                    (int) (horizonRollTexture.getWidth() / 2.f),
                    (int) (horizonRollTexture.getHeight() / 2.f) - 150,
                    (int) (horizonRollTexture.getWidth() / 2.f),
                    (int) (horizonRollTexture.getHeight() / 2.f) - 150 + 10
                    );
            g.transform(identity);
            g.setStroke(new BasicStroke());
        }
    }
}
