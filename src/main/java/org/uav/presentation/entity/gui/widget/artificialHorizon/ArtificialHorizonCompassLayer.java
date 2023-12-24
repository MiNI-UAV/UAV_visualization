package org.uav.presentation.entity.gui.widget.artificialHorizon;

import org.joml.Matrix3x2f;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.state.controlMode.ControlModeReply;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.entity.vector.VectorTriangle;
import org.uav.presentation.entity.vector.VectorVertex;
import org.uav.presentation.rendering.Shader;
import org.uav.utils.Convert;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ArtificialHorizonCompassLayer {
    private final BufferedImage compassTexture;
    private final Sprite compassSprite;
    private final VectorTriangle compassCursorShape;
    private final VectorTriangle demandedCompassCursorShape;
    private int compassOffset;
    private final int horizonScreenX;
    private float demandedRotZ;
    private boolean drawDemandedRotZ;

    public ArtificialHorizonCompassLayer(
            BufferedImage compassTexture,
            int horizonScreenX,
            Shader spriteShader,
            Shader vectorShader
    ) {
        this.compassTexture = compassTexture;
        this.horizonScreenX = horizonScreenX;
        compassSprite = new Sprite(compassTexture, spriteShader);
        var compassCursor = List.of(
                new VectorVertex(0.0f, 0.92f),
                new VectorVertex(0.02f, 0.84f),
                new VectorVertex(-0.02f, 0.84f)
        );
        compassCursorShape = new VectorTriangle(compassCursor, vectorShader);
        var demandedCompassCursor = List.of(
                new VectorVertex(0.0f, 0.90f),
                new VectorVertex(0.01f, 0.84f),
                new VectorVertex(-0.01f, 0.84f)
        );
        demandedCompassCursorShape = new VectorTriangle(demandedCompassCursor, vectorShader);
        drawDemandedRotZ = false;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isEmpty()) return;
        var rotation = Convert.toEuler(drone.get().droneStatus.rotation);
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
            demandedRotZ = (float) ((compassOffset - ((demanded + Math.PI) / (2 * Math.PI) * 1440f)) / (horizonScreenX/2f));
            drawDemandedRotZ = true;
        }
    }

    public void draw(MemoryStack stack) {
        var transform = new Matrix3x2f();
        transform.translate((float) compassOffset / compassTexture.getWidth(), 0);
        transform.scale((float) horizonScreenX / compassTexture.getWidth() ,1);
        compassSprite.setTransform(transform);
        compassSprite.draw(stack);

        compassCursorShape.draw(stack);
        drawDemanded(stack);
    }

    private void drawDemanded(MemoryStack stack) {
        if(drawDemandedRotZ) {
            var transform = new Matrix3x2f();
            transform.translate(-demandedRotZ, 0);
            demandedCompassCursorShape.setColor(Color.RED);
            demandedCompassCursorShape.setTransform(transform);
            demandedCompassCursorShape.draw(stack);
        }
    }
}
