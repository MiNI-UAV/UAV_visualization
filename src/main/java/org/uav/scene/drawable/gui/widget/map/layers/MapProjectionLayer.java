package org.uav.scene.drawable.gui.widget.map.layers;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.uav.model.SimulationState;
import org.uav.model.controlMode.ControlModeReply;
import org.uav.scene.drawable.gui.DrawableGuiLayer;
import org.uav.utils.Convert;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class MapProjectionLayer implements DrawableGuiLayer {
    private final Vector2f mapScale;
    private final float velocityScale;
    private final BufferedImage mapImage;
    private final BufferedImage droneImage;
    private final BufferedImage droneImageDemanded;
    private final Vector2i mapResolution;
    private float mapZoom;
    private float rotZ;
    private Vector2f dronePosition;
    private Vector2f droneVelocity;
    private Vector2f demandedPosition;
    private float demandedRotZ;
    private boolean drawDemandedPositional;

    public MapProjectionLayer(BufferedImage mapImage, BufferedImage droneImage, BufferedImage droneImageDemanded, Vector2f mapScale, Vector2i mapResolution) {
        this.mapImage = mapImage;
        this.droneImage = droneImage;
        this.droneImageDemanded = droneImageDemanded;
        this.mapResolution = mapResolution;
        rotZ = 0;
        this.mapScale = mapScale;
        velocityScale = 10; // TODO Based on max rpms and msp resolution
        dronePosition = new Vector2f();
        droneVelocity = new Vector2f();
        demandedPosition = new Vector2f();
        demandedRotZ = 0;
        drawDemandedPositional = false;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().getId());
        if(drone == null) return;
        var rotation = Convert.toEuler(drone.rotation);
        rotZ = rotation.z;
        dronePosition = new Vector2f(-drone.position.y, drone.position.x).mul(mapScale);
        droneVelocity = new Vector2f(drone.linearVelocity.y, -drone.linearVelocity.x).mul(velocityScale);
        mapZoom = simulationState.getMapZoom();
        updateDemanded(simulationState);
    }

    private void updateDemanded(SimulationState simulationState) {
        if(
                simulationState.getCurrentControlModeDemanded() == null ||
                !simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.X) ||
                !simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.Y) ||
                !simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.YAW)
        )
            drawDemandedPositional = false;
        else {
            float demandedX = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.X);
            float demandedY = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.Y);
            demandedRotZ = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.YAW);
            demandedPosition = new Vector2f(-demandedY, demandedX).mul(mapScale);
            drawDemandedPositional = true;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        drawMap(g);
        drawDemanded(g);
        drawDrone(g);
    }

    private void drawDrone(Graphics2D g) {
        AffineTransform trans = new AffineTransform();
        trans.translate(mapResolution.x / 2f, mapResolution.y / 2f);
        trans.rotate(rotZ);
        trans.translate(-droneImage.getWidth() / 2f, - droneImage.getHeight() / 2f);
        g.drawImage(droneImage, trans, null);

        g.setColor(Color.yellow);
        g.setStroke(new BasicStroke(2));
        g.drawLine(
                mapResolution.x / 2,
                mapResolution.y / 2,
                (int) (mapResolution.x / 2 + droneVelocity.x),
                (int) (mapResolution.y / 2 + droneVelocity.y)
        );
        g.setStroke(new BasicStroke());
    }

    private void drawMap(Graphics2D g) {
        var oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.7f ));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(
                mapImage,
                0,
                0,
                mapResolution.x,
                mapResolution.y,
                (int) (mapImage.getWidth() / 2f - dronePosition.x - mapResolution.x / 2f / mapZoom),
                (int) (mapImage.getHeight() / 2f - dronePosition.y - mapResolution.y / 2f / mapZoom),
                (int) (mapImage.getWidth() / 2f - dronePosition.x + mapResolution.x / 2f / mapZoom),
                (int) (mapImage.getHeight() / 2f - dronePosition.y + mapResolution.y / 2f / mapZoom),
                null
        );
        g.setComposite(oldComposite);
    }

    private void drawDemanded(Graphics2D g) {
        if(drawDemandedPositional) {
            g.setColor(new Color(1, 0, 0, 0.2f));
            g.setStroke(new BasicStroke(2));
            g.drawLine(
                    (int) (mapResolution.x / 2f),
                    (int) (mapResolution.y / 2f),
                    (int) (mapResolution.x / 2f + (dronePosition.x - demandedPosition.x) * mapZoom),
                    (int) (mapResolution.y / 2f + (dronePosition.y - demandedPosition.y) * mapZoom)
            );
            g.setStroke(new BasicStroke());

            var oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.3f ));
            AffineTransform trans = new AffineTransform();
            trans.translate(mapResolution.x / 2f, mapResolution.y / 2f);
            trans.translate((dronePosition.x - demandedPosition.x) * mapZoom, (dronePosition.y - demandedPosition.y) * mapZoom);
            trans.rotate(demandedRotZ);
            trans.translate(-droneImageDemanded.getWidth() / 2f, - droneImageDemanded.getHeight() / 2f);
            g.drawImage(droneImageDemanded, trans, null);
            g.setComposite(oldComposite);
        }

    }
}
