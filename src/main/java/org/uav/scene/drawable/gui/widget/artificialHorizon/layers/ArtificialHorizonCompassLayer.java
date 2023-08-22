package org.uav.scene.drawable.gui.widget.artificialHorizon.layers;

import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.DrawableGuiLayer;
import org.uav.utils.Convert;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ArtificialHorizonCompassLayer implements DrawableGuiLayer {
    private final BufferedImage compassTexture;
    private final Polygon compassCursor;
    private final int compassWidth;
    private int compassOffset;

    public ArtificialHorizonCompassLayer(
            BufferedImage compassTexture,
            int compassHeight,
            int horizonScreenX
    ) {
        this.compassTexture = compassTexture;
        compassWidth = compassTexture.getWidth();
        compassCursor = new Polygon(
                new int[] {horizonScreenX / 2 - 3, horizonScreenX / 2 + 3, horizonScreenX / 2},
                new int[] {compassHeight, compassHeight, compassHeight - 10},
                3
        );
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().getId());
        if(drone == null) return;
        var rotation = Convert.toEuler(drone.rotation);
        compassOffset = (int) ((rotation.z+Math.PI)/(2*Math.PI) * 1440);
    }
    @Override
    public void draw(Graphics2D g) {
        g.drawImage(compassTexture,
                0, 0, compassTexture.getWidth(), compassTexture.getHeight(),
                compassOffset, 0, compassOffset + compassWidth, compassTexture.getHeight(),  null);
        g.fillPolygon(compassCursor);
    }
}
