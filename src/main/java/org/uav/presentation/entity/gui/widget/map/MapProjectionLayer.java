package org.uav.presentation.entity.gui.widget.map;

import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.uav.logic.state.controlMode.ControlModeReply;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.entity.vector.VectorRectangle;
import org.uav.presentation.entity.vector.VectorVertex;
import org.uav.presentation.rendering.Shader;
import org.uav.utils.Convert;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class MapProjectionLayer {
    private final Vector2f mapScale;
    private final BufferedImage mapImage;
    private final Sprite mapSprite;
    private final BufferedImage droneImage;
    private final Sprite droneSprite;
    private final BufferedImage droneImageDemanded;
    private final Sprite droneDemandedSprite;
    private final VectorRectangle velocityShape;
    private final Vector2i mapResolution;
    private float mapZoom;
    private float rotZ;
    private Vector2f dronePosition;
    private Vector2f droneVelocity;
    private Vector2f demandedPosition;
    private float demandedRotZ;
    private boolean drawDemandedPositional;

    public MapProjectionLayer(BufferedImage mapImage, BufferedImage droneImage, BufferedImage droneImageDemanded, Vector2f mapScale, Vector2i mapResolution, Shader spriteShader, Shader vectorShader) {
        this.mapImage = mapImage;
        mapSprite = new Sprite(mapImage, spriteShader);
        this.droneImage = droneImage;
        droneSprite = new Sprite(droneImage, spriteShader);
        this.droneImageDemanded = droneImageDemanded;
        droneDemandedSprite = new Sprite(droneImageDemanded, spriteShader);
        this.mapResolution = mapResolution;
        rotZ = 0;
        this.mapScale = mapScale;
        dronePosition = new Vector2f();
        droneVelocity = new Vector2f();
        demandedPosition = new Vector2f();
        demandedRotZ = 0;
        drawDemandedPositional = false;

        float lineThickness = 0.005f;
        float defaultLineLength = 0.01f;
        var quad = List.of(
                new VectorVertex(0.0f - lineThickness/2, 0.0f + defaultLineLength),
                new VectorVertex(0.0f + lineThickness/2, 0.0f + defaultLineLength),
                new VectorVertex(0.0f + lineThickness/2, 0.0f),
                new VectorVertex(0.0f - lineThickness/2, 0.0f)
        );
        velocityShape = new VectorRectangle(quad, vectorShader);
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isEmpty()) return;
        var droneStatus = drone.get().droneStatus;
        var rotation = Convert.toEuler(droneStatus.rotation);
        rotZ = -rotation.z;
        dronePosition = new Vector2f(-droneStatus.position.y, droneStatus.position.x).mul(mapScale);
        droneVelocity = new Vector2f(droneStatus.linearVelocity.y, droneStatus.linearVelocity.x).negate();
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

    public void draw() {
        drawMap();
        drawDemanded();
        drawDrone();
    }

    private void drawDrone() {

        var transform = new Matrix3x2f();
        transform.translate(0.5f , 0.5f);
        transform.rotate(rotZ);
        transform.translate(- (float) mapResolution.x / droneImage.getWidth() / 2,  - (float) mapResolution.y / droneImage.getHeight() / 2);
        transform.scale((float) mapResolution.x / droneImage.getWidth(), (float) mapResolution.y / droneImage.getHeight());
        droneSprite.setTransform(transform);
        droneSprite.draw();

        transform = new Matrix3x2f();
        transform.rotate((float) (Math.atan2(droneVelocity.y, droneVelocity.x) + Math.PI / 2));
        transform.scale(1, droneVelocity.length());
        velocityShape.setColor(Color.YELLOW);
        velocityShape.setTransform(transform);
        velocityShape.draw();
    }

    private void drawMap() {
        var transform = new Matrix3x2f();
        transform.translate(0.5f - dronePosition.x / mapImage.getWidth() - mapResolution.x / 2f / mapZoom / mapImage.getWidth(), 0.5f - dronePosition.y /mapImage.getHeight() - mapResolution.y / 2f / mapZoom / mapImage.getHeight());
        transform.scale((float) mapResolution.x / mapImage.getWidth() / mapZoom, (float) mapResolution.y / mapImage.getHeight() / mapZoom);
        mapSprite.setTransform(transform);
        mapSprite.setOpacity(0.7f);
        mapSprite.draw();
    }

    private void drawDemanded() {
        if(drawDemandedPositional) {
            var transform = new Matrix3x2f();
            transform.translate(0.5f , 0.5f);
            transform.rotate(-demandedRotZ);
            transform.translate(-(dronePosition.x - demandedPosition.x) / droneImage.getWidth() * mapZoom, -(dronePosition.y - demandedPosition.y) /droneImage.getHeight() * mapZoom);
            transform.translate(- (float) mapResolution.x / droneImage.getWidth() / 2,  - (float) mapResolution.y / droneImage.getHeight() / 2);
            transform.scale((float) mapResolution.x / droneImage.getWidth(), (float) mapResolution.y / droneImage.getHeight());
            droneDemandedSprite.setOpacity(0.3f);
            droneDemandedSprite.setTransform(transform);
            droneDemandedSprite.draw();

        }

    }
}
