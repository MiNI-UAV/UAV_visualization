package org.uav.presentation.entity.gui.widget.artificialHorizon;

import org.joml.Matrix3x2f;
import org.joml.Vector2f;
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

public class ArtificialHorizonBackgroundLayer {
    private final BufferedImage horizonTexture;
    private final Sprite horizontSprite;
    private final VectorRectangle demandedPitchShape;
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
            int horizonScreenY,
            Shader spriteShader,
            Shader vectorShader
    ) {
        this.horizonTexture = horizonTexture;
        this.distanceToMax = distanceToMax;
        this.horizonScreenX = horizonScreenX;
        this.horizonScreenY = horizonScreenY;
        horizontSprite = new Sprite(horizonTexture, spriteShader);

        float lineThickness = 0.02f;
        float lineLength = 0.2f;
        var quad = List.of(
                new VectorVertex(0.0f - lineLength/2, 0.0f + lineThickness/2),
                new VectorVertex(0.0f + lineLength/2, 0.0f + lineThickness/2),
                new VectorVertex(0.0f + lineLength/2, 0.0f - lineThickness/2),
                new VectorVertex(0.0f - lineLength/2, 0.0f - lineThickness/2)
        );
        demandedPitchShape = new VectorRectangle(quad, vectorShader);
        rotX = 0;
        rotY = 0;
        drawDemandedRotY = false;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isEmpty()) return;
        var rotation = Convert.toEuler(drone.get().droneStatus.rotation);
        rotX = rotation.x;
        rotY = rotation.y / (0.5f * (float) Math.PI) * distanceToMax / ((float) horizonTexture.getHeight() / 2) / 2;
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
            demandedRotY = (rotY - (demanded / (0.5f * (float) Math.PI) * distanceToMax / ((float) horizonTexture.getHeight() / 2) / 2)) *(float) horizonTexture.getHeight()/ horizonScreenY * 2;
            drawDemandedRotY = true;
        }
    }

    public void draw() {
        var transform = new Matrix3x2f();
        var viewRatio = new Vector2f((float) horizonScreenX / horizonScreenY, 1);
        var inverseViewRatio = new Vector2f((float) horizonScreenY / horizonScreenX, 1);
        var scaleTexture = new Vector2f((float) horizonScreenY / horizonTexture.getWidth() * viewRatio.x, (float) horizonScreenY / horizonTexture.getHeight());
        var translateTexture = new Vector2f(0.5f - (float) horizonScreenY / horizonTexture.getWidth() / 2 * viewRatio.x, 0.5f - (float) horizonScreenY / horizonTexture.getHeight() / 2);
        var moveByPitch = new Vector2f(0, -rotY);

        transform.translate(moveByPitch);
        transform.translate(translateTexture);
        transform.scale(scaleTexture);
        transform.scale(inverseViewRatio);
        transform.rotateAbout(rotX, 0.5f * viewRatio.x,0.5f);
        transform.scale(viewRatio);

        horizontSprite.setTransform(transform);
        horizontSprite.draw();

        drawDemanded();
    }

    private void drawDemanded() {
        var viewRatio = new Vector2f((float) horizonScreenX / horizonScreenY, 1);
        var inverseViewRatio = new Vector2f((float) horizonScreenY / horizonScreenX, 1);
        if(drawDemandedRotY) {
            var transform = new Matrix3x2f();
            transform.scale(inverseViewRatio);
            transform.rotateAbout(rotX, 0, 0);
            transform.scale(viewRatio);
            transform.translate(0, -demandedRotY);
            demandedPitchShape.setColor(Color.RED);
            demandedPitchShape.setTransform(transform);
            demandedPitchShape.draw();
        }
    }
}
