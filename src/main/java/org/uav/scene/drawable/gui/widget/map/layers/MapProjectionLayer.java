package org.uav.scene.drawable.gui.widget.map.layers;

import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.DrawableGuiLayer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static java.lang.Math.atan2;

public class MapProjectionLayer implements DrawableGuiLayer {
    private final static AffineTransform identity = new AffineTransform();
    private final float mapScale;
    private final float velocityScale;
    private final BufferedImage mapImage;
    private final BufferedImage droneImage;
    private final Vector2i mapResolution;
    private float rotZ;
    private Vector2f dronePosition;
    private Vector2f droneVelocity;

    public MapProjectionLayer(BufferedImage mapImage, BufferedImage droneImage, float mapScale, Vector2i mapResolution) {
        this.mapImage = mapImage;
        this.droneImage = droneImage;
        this.mapResolution = mapResolution;
        rotZ = 0;
        this.mapScale = mapScale;
        velocityScale = 10;
        dronePosition = new Vector2f();
        droneVelocity = new Vector2f();
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().id);
        if(drone == null) return;
        Quaternionf q = drone.rotation;
        rotZ = ((float) atan2(2 * (q.w * q.z + q.x * q.y), 1 - 2 * (q.y*q.y + q.z*q.z)));
        dronePosition = new Vector2f(-drone.position.y, drone.position.x).mul(mapScale);
        droneVelocity = new Vector2f(drone.linearVelocity.y, -drone.linearVelocity.x).mul(velocityScale);
    }

    @Override
    public void draw(Graphics2D g) {
        AffineTransform trans = new AffineTransform();
        trans.setTransform(identity);
        trans.translate(dronePosition.x, dronePosition.y);
        g.drawImage(mapImage, trans, null);

        trans = new AffineTransform();
        trans.setTransform(identity);
        trans.translate(mapResolution.x / 2f, mapResolution.y / 2f);
        trans.rotate(rotZ);
        trans.translate(-droneImage.getWidth() / 2f, - droneImage.getHeight() / 2f);
        g.drawImage(droneImage, trans, null);

        g.setColor(Color.red);
        g.drawLine(
                mapResolution.x / 2,
                mapResolution.y / 2,
                (int) (mapResolution.x / 2 + droneVelocity.x),
                (int) (mapResolution.y / 2 + droneVelocity.y)
        );
    }
}
