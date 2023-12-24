package org.uav.presentation.entity.gui.widget.artificialHorizon;

import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;
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

public class ArtificialHorizonRollLayer {
    private final Sprite horizonRollSprite;
    private final VectorRectangle demandedRollShape;
    private float rotX;
    private float demandedRotX;
    private boolean drawDemandedRotX;
    private final Vector2f toViewScale;
    private final Vector2f fromViewScale;

    public ArtificialHorizonRollLayer(
            BufferedImage horizonRollTexture,
            Shader spriteShader,
            Shader vectorShader,
            float aspectRatio
    ) {
        drawDemandedRotX = false;
        horizonRollSprite = new Sprite(horizonRollTexture, spriteShader);
        toViewScale = new Vector2f(1/aspectRatio, 1);
        fromViewScale = new Vector2f(aspectRatio, 1);

        float lineThickness = 0.02f;
        var quad = List.of(
                new VectorVertex(0.0f - lineThickness/2, 0.84f),
                new VectorVertex(0.0f + lineThickness/2, 0.84f),
                new VectorVertex(0.0f + lineThickness/2, 0.78f),
                new VectorVertex(0.0f - lineThickness/2, 0.78f)
        );
        demandedRollShape = new VectorRectangle(quad, vectorShader);
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isEmpty()) return;
        var rotation = Convert.toEuler(drone.get().droneStatus.rotation);
        rotX = rotation.x;
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
            demandedRotX = rotX - demanded;
            drawDemandedRotX = true;
        }
    }

    public void draw(MemoryStack stack) {
        var transform = new Matrix3x2f();
        transform.scale(fromViewScale);
        transform.rotateAbout(rotX, 0.5f * toViewScale.x,0.5f);
        transform.scale(toViewScale);

        horizonRollSprite.setTransform(transform);
        horizonRollSprite.draw(stack);

        drawDemanded(stack);
    }

    private void drawDemanded(MemoryStack stack) {
        if(drawDemandedRotX) {
            var transform = new Matrix3x2f();
            transform.scale(fromViewScale);
            transform.rotate(demandedRotX);
            transform.scale(toViewScale);

            demandedRollShape.setTransform(transform);
            demandedRollShape.setColor(new Color(1, 0, 0, 1f));
            demandedRollShape.draw(stack);
        }
    }
}
