package org.uav.scene.drawable.gui.widget.radar;

import org.joml.Vector2f;
import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.model.status.DroneStatus;
import org.uav.scene.drawable.GuiWidget;
import org.uav.scene.drawable.gui.GuiAnchorPoint;
import org.uav.scene.drawable.gui.GuiElement;
import org.uav.scene.drawable.gui.widget.radar.layers.RadarArrowLayer;
import org.uav.scene.drawable.gui.widget.radar.layers.RadarPointsLayer;
import org.uav.scene.shader.Shader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class RadarWidget implements GuiWidget {
    private static final float FULL_CIRCUMFERENCE = (float) (2 * Math.PI);
    private final GuiElement guiElement;
    private final RadarArrowLayer radarArrowLayer;
    private final RadarPointsLayer radarPointsLayer;
    private final SimulationState simulationState;
    private final float radarArrowSpeed;
    private float radarArrowAngle;
    private final List<RadarPoint> radarPoints;
    private final int startingTraceStrength;
    private final float radarRangeRadiusSquared;
    private final float radarSectorLength;
    private float currentSectorAngle;
    public RadarWidget(BufferedImage radarTexture, BufferedImage radarArrowTexture, SimulationState simulationState, Config config) {
        this.simulationState = simulationState;

        radarArrowSpeed = (float) (1/30f * Math.PI);
        radarArrowAngle = 0;
        radarPoints = new ArrayList<>();
        startingTraceStrength = 40;
        float radarRangeRadius = 45; // TODO Adjust to size of the compass
        radarRangeRadiusSquared = radarRangeRadius * radarRangeRadius;
        radarSectorLength = (float) (1/18f * Math.PI);
        currentSectorAngle = 0;
        int radarPointsCanvasX = 100;
        int radarPointsCanvasY = 100;

        radarArrowLayer = new RadarArrowLayer(radarArrowTexture);
        radarPointsLayer = new RadarPointsLayer(radarPoints, startingTraceStrength, radarRangeRadius, radarPointsCanvasX);

        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(-0.5f, -1.0f, -1.0f, -0.5f)
                .setAnchorPoint(GuiAnchorPoint.BOTTOM_LEFT)
                .setScale(config.guiScale)
                .setResolution(config.windowWidth, config.windowHeight)
                .setHidden(false)
                .addLayer(radarTexture)
                .addLayer(radarArrowTexture.getWidth(), radarArrowTexture.getHeight(), radarArrowLayer)
                .addLayer(radarPointsCanvasX, radarPointsCanvasY, radarPointsLayer)
                .build();
    }

    public void update() {
        // TODO: Make radar angle dependent on global clock not on FPS;
        if(guiElement.getHidden()) return;
        radarArrowAngle += radarArrowSpeed;
        if(radarArrowAngle > currentSectorAngle) {
            activateRadar();
            while(radarArrowAngle > currentSectorAngle)
                currentSectorAngle += radarSectorLength;
            if(currentSectorAngle > FULL_CIRCUMFERENCE)
                currentSectorAngle = 0;
        }
        if(radarArrowAngle > FULL_CIRCUMFERENCE)
            radarArrowAngle = 0;
        radarArrowLayer.update(radarArrowAngle);
    }

    private void activateRadar() {
        // Test for drone position between (currentSectorAngle - radarSectorLength/2) and (currentSectorAngle+- radarSectorLength/2)
        radarPoints.forEach(point -> point.traceStrength--);
        radarPoints.removeIf(point -> point.traceStrength <= 0);

        var drones = simulationState.getCurrPassDroneStatuses().map;
        var radarDrone = drones.get(simulationState.getCurrentlyControlledDrone().id);
        if(radarDrone == null) return;
        List<RadarPoint> newPoints = drones.values().stream()
                .filter(drone -> drone != radarDrone)
                .map(drone -> getRelativePoints(drone, radarDrone))
                .filter(this::isInsideSector)
                .map(coordinates -> new RadarPoint(coordinates, startingTraceStrength))
                .toList();
        radarPoints.addAll(newPoints);
    }

    private static Vector2f getRelativePoints(DroneStatus drone, DroneStatus radarDrone) {
        float relX = drone.position.x - radarDrone.position.x;
        float relY = drone.position.y - radarDrone.position.y;
        float angle = -radarDrone.rotation.z;
        return rotatePoint(angle, new Vector2f(relX, relY));
    }

    private static Vector2f rotatePoint(float angle, Vector2f point) {
        float sinAngle = (float) Math.sin(angle);
        float cosAngle = (float) Math.cos(angle);
        return new Vector2f(point.x * cosAngle - point.y * sinAngle, point.x * sinAngle + point.y * cosAngle);
    }

    private boolean isInsideSector(Vector2f relPoint) {
        float sectorStartAngle = currentSectorAngle - radarSectorLength / 2;
        float sectorEndAngle = currentSectorAngle + radarSectorLength / 2;
        float angleToDrone = (float) Math.atan2(relPoint.y, relPoint.x);
        if(angleToDrone <0)
            angleToDrone += 2 * (float) Math.PI;


        return sectorStartAngle < angleToDrone &&
                angleToDrone < sectorEndAngle &&
                isWithinRadius(relPoint, radarRangeRadiusSquared);
    }

    private boolean isWithinRadius(Vector2f v, float radiusSquared) {
        return v.x*v.x + v.y*v.y <= radiusSquared;
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }
}