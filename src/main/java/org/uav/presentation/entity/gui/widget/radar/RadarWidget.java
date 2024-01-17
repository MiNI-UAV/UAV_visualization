package org.uav.presentation.entity.gui.widget.radar;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.uav.logic.config.Config;
import org.uav.logic.state.drone.DroneStatus;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.rendering.Shader;
import org.uav.utils.Convert;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class RadarWidget extends Widget {
    private static final float FULL_CIRCUMFERENCE = (float) (2 * Math.PI);
    private final Sprite radarSprite;
    private final RadarArrowLayer radarArrowLayer;
    private final RadarPointsLayer radarPointsLayer;
    private final float radarArrowSpeed;
    private float radarArrowAngle;
    private final List<RadarPoint> radarPoints;
    private final int startingTraceStrength;
    private final float radarRangeRadiusSquared;
    private final float radarSectorLength;
    private float currentSectorAngle;
    public RadarWidget(BufferedImage radarTexture, BufferedImage radarArrowTexture, Shader spriteShader, Shader vectorShader, Config config) {
        super(getWidgetPosition(), GuiAnchorPoint.BOTTOM_LEFT, config);

        radarArrowSpeed = (float) (1/30f * Math.PI);
        radarArrowAngle = 0;
        radarPoints = new ArrayList<>();
        startingTraceStrength = 40;
        float radarRangeRadius = 45;
        radarRangeRadiusSquared = radarRangeRadius * radarRangeRadius;
        radarSectorLength = (float) (1/18f * Math.PI);
        currentSectorAngle = 0;
        int radarPointsCanvasX = 100;
        int radarPointsCanvasY = 100;

        radarSprite = new Sprite(radarTexture, spriteShader);
        radarArrowLayer = new RadarArrowLayer(radarArrowTexture, spriteShader);
        radarPointsLayer = new RadarPointsLayer(radarPoints, startingTraceStrength, radarRangeRadius, radarPointsCanvasX, vectorShader);
        }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(-0.5f, -1.0f, -1.0f, -0.5f);
    }

    public void update(SimulationState simulationState) {
        // TODO: Make radar angle dependent on global clock not on FPS;
//        if(guiElement.getHidden()) return;
        radarArrowAngle += radarArrowSpeed;
        if(radarArrowAngle > currentSectorAngle) {
            activateRadar(simulationState);
            while(radarArrowAngle > currentSectorAngle)
                currentSectorAngle += radarSectorLength;
            if(currentSectorAngle > FULL_CIRCUMFERENCE)
                currentSectorAngle = 0;
        }
        if(radarArrowAngle > FULL_CIRCUMFERENCE)
            radarArrowAngle = 0;
        radarArrowLayer.update(radarArrowAngle);
    }

    private void activateRadar(SimulationState simulationState) {
        // Test for drone position between (currentSectorAngle - radarSectorLength/2) and (currentSectorAngle+- radarSectorLength/2)
        radarPoints.forEach(point -> point.traceStrength--);
        radarPoints.removeIf(point -> point.traceStrength <= 0);
        var drones = simulationState.getDronesInAir();
        var radarDrone = simulationState.getPlayerDrone();
        if(radarDrone.isEmpty())
        {    
            return;
        }
        List<RadarPoint> newPoints = drones.values().stream()
                .filter(drone -> drone != radarDrone.get())
                .map(drone -> getRelativePoints(drone.droneStatus, radarDrone.get().droneStatus))
                .filter(this::isInsideSector)
                .map(coordinates -> new RadarPoint(coordinates, startingTraceStrength))
                .toList();
        radarPoints.addAll(newPoints);
    }

    private static Vector2f getRelativePoints(DroneStatus drone, DroneStatus radarDrone) {
        float relX = drone.position.x - radarDrone.position.x;
        float relY = drone.position.y - radarDrone.position.y;
        var rotation = Convert.toEuler(radarDrone.rotation);
        return rotatePoint(-rotation.z, new Vector2f(relX, relY));
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


        return sectorStartAngle <= angleToDrone &&
                angleToDrone <= sectorEndAngle &&
                isWithinRadius(relPoint, radarRangeRadiusSquared);
    }

    private boolean isWithinRadius(Vector2f v, float radiusSquared) {
        return v.x*v.x + v.y*v.y <= radiusSquared;
    }

    @Override
    protected void drawWidget() {
        radarSprite.draw();
        radarPointsLayer.draw();
        radarArrowLayer.draw();
    }
}
