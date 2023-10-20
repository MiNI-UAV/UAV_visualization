package org.uav.scene.gui.widget.artificialHorizon.layers;

import org.uav.model.SimulationState;
import org.uav.model.controlMode.ControlModeReply;
import org.uav.scene.gui.DrawableGuiLayer;
import org.uav.utils.Convert;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ArtificialHorizonCompassLayer implements DrawableGuiLayer {
    private final BufferedImage compassTexture;
    private final Polygon compassCursor;
    private final int compassWidth;
    private int compassOffset;
    private final int horizonScreenX;
    private float demandedRotZ;
    private boolean drawDemandedRotZ;

    public ArtificialHorizonCompassLayer(
            BufferedImage compassTexture,
            int compassHeight,
            int horizonScreenX
    ) {
        this.compassTexture = compassTexture;
        this.horizonScreenX = horizonScreenX;
        compassWidth = compassTexture.getWidth();
        compassCursor = new Polygon(
                new int[] {horizonScreenX / 2 - 3, horizonScreenX / 2 + 3, horizonScreenX / 2},
                new int[] {compassHeight, compassHeight, compassHeight - 10},
                3
        );
        drawDemandedRotZ = false;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().getId());
        if(drone == null) return;
        var rotation = Convert.toEuler(drone.rotation);
        compassOffset = (int) ((rotation.z+Math.PI)/(2*Math.PI) * 1440);

        updateDemanded(simulationState);
    }

    private void updateDemanded(SimulationState simulationState) {
        if(
            simulationState.getCurrentControlModeDemanded() == null ||
            !simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.YAW)
        )
            drawDemandedRotZ = false;
        else {
            float demanded = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.YAW);
            demandedRotZ = compassOffset - (int) ((demanded+Math.PI)/(2*Math.PI) * 1440);
            drawDemandedRotZ = true;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.drawImage(compassTexture,
                0, 0, compassTexture.getWidth(), compassTexture.getHeight(),
                compassOffset, 0, compassOffset + compassWidth, compassTexture.getHeight(),  null);
        g.fillPolygon(compassCursor);
        drawDemanded(g);
    }

    private void drawDemanded(Graphics2D g) {
        if(drawDemandedRotZ) {
            g.setStroke(new BasicStroke(5));
            g.setColor(new Color(1,0,0,0.5f));
            g.drawLine((int) (horizonScreenX / 2.f - demandedRotZ), 0, (int) (horizonScreenX / 2.f - demandedRotZ), 10);
            g.setStroke(new BasicStroke());
        }
    }
}
