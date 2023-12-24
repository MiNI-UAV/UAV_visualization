package org.uav.presentation.entity.gui.widget.artificialHorizon;

import org.joml.*;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.config.Config;
import org.uav.logic.state.controlMode.ControlModeDemanded;
import org.uav.logic.state.controlMode.ControlModeReply;
import org.uav.logic.state.drone.DroneStatus;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.text.TextEngine;
import org.uav.presentation.entity.vector.VectorRectangle;
import org.uav.presentation.entity.vector.VectorTriangle;
import org.uav.presentation.entity.vector.VectorVertex;
import org.uav.presentation.rendering.Shader;

import java.awt.*;
import java.lang.Math;
import java.text.MessageFormat;
import java.util.List;

import static org.uav.utils.OpenGLUtils.*;
import static org.uav.utils.SignificantDigitsRounder.sigDigRounder;

public class ArtificialHorizonMetersLayer {
    private static final int Z_AXIS_INVERSION = -1;
    private static final Color BG_COLOR = new Color(0, 0, 0, 32);
    private static final float FONT_SIZE_NORM = 35f / 1080;
    private static final float STRING_DRAWING_MARGIN = (FONT_SIZE_NORM + 1) / 2;
    private static final float METER_UNIT_HEIGHT = 15;
    private static final float METER_PRECISION = 1;
    private static final float NUMBER_PRECISION = 2 * METER_PRECISION;
    private final int horizonScreenX;
    private final int horizonScreenY;
    private Vector2f position;
    private float velocity;
    private float climbRate;
    private float height;
    private ControlModeDemanded controlModeDemanded;
    private float demandedHeight;
    private boolean drawDemandedHeight;
    private float demandedVelocityX;
    private boolean drawDemandedVelocityX;


    // New GUI
    private VectorRectangle meterBackgroundShape;
    private VectorRectangle meterStripeShape;
    private VectorRectangle meterBarsShape;
    private VectorTriangle meterCursorShape;
    private VectorTriangle demandedMeterCursorShape;

    // Meter Data

    float meterHeightNorm;
    float meterWidthNorm;
    private final float stripeWidthNorm;
    private final float barHeightNorm;
    private final float barWidthNorm;
    private final float cursorHeightNorm;
    private final float cursorWidthNorm;
    private final float demandedCursorWidthNorm;
    private final float demandedCursorHeightNorm;
    private final TextEngine textEngine;

    public ArtificialHorizonMetersLayer(
            int horizonScreenX,
            int horizonScreenY,
            Vector4f widgetPosition,
            Shader vectorShader,
            Shader textShader,
            Config config
    ) {
        this.horizonScreenX = horizonScreenX;
        this.horizonScreenY = horizonScreenY;
        meterHeightNorm = 1f / 2 * OPENGL_CANVAS_SIZE;
        meterWidthNorm = 1f / 8 * OPENGL_CANVAS_SIZE;
        position = new Vector2f();
        velocity = 0;
        climbRate = 0;
        height = 0;
        drawDemandedHeight = false;
        drawDemandedVelocityX = false;
        textEngine = new TextEngine(widgetPosition, FONT_SIZE_NORM, textShader, config);

        var rectangle = List.of(
                new VectorVertex(0.0f - meterWidthNorm/ 2, 0.0f + meterHeightNorm / 2),
                new VectorVertex(0.0f + meterWidthNorm/ 2, 0.0f + meterHeightNorm / 2),
                new VectorVertex(0.0f + meterWidthNorm/ 2, 0.0f - meterHeightNorm / 2),
                new VectorVertex(0.0f - meterWidthNorm/ 2, 0.0f - meterHeightNorm / 2)
        );
        meterBackgroundShape = new VectorRectangle(rectangle, vectorShader);
        meterBackgroundShape.setColor(BG_COLOR);

        stripeWidthNorm = 2f / 480 * OPENGL_CANVAS_SIZE;
        rectangle = List.of(
                new VectorVertex(0.0f - stripeWidthNorm/ 2, 0.0f + meterHeightNorm / 2),
                new VectorVertex(0.0f + stripeWidthNorm/ 2, 0.0f + meterHeightNorm / 2),
                new VectorVertex(0.0f + stripeWidthNorm/ 2, 0.0f - meterHeightNorm / 2),
                new VectorVertex(0.0f - stripeWidthNorm/ 2, 0.0f - meterHeightNorm / 2)
        );
        meterStripeShape = new VectorRectangle(rectangle, vectorShader);

        barWidthNorm = 8f / 480 * OPENGL_CANVAS_SIZE;
        barHeightNorm = 2f / 360 * OPENGL_CANVAS_SIZE;
        rectangle = List.of(
                new VectorVertex(0.0f - barWidthNorm/ 2, 0.0f + barHeightNorm / 2),
                new VectorVertex(0.0f + barWidthNorm/ 2, 0.0f + barHeightNorm / 2),
                new VectorVertex(0.0f + barWidthNorm/ 2, 0.0f - barHeightNorm / 2),
                new VectorVertex(0.0f - barWidthNorm/ 2, 0.0f - barHeightNorm / 2)
        );
        meterBarsShape = new VectorRectangle(rectangle, vectorShader);

        cursorWidthNorm = barWidthNorm * 2;
        cursorHeightNorm = barHeightNorm * 4;
        var triangle = List.of(
                new VectorVertex(0.0f + cursorWidthNorm / 2, 0.0f),
                new VectorVertex(0.0f - cursorWidthNorm / 2, 0.0f + cursorHeightNorm / 2),
                new VectorVertex(0.0f - cursorWidthNorm / 2, 0.0f - cursorHeightNorm / 2)
        );
        meterCursorShape = new VectorTriangle(triangle , vectorShader);

        demandedCursorWidthNorm = cursorWidthNorm / 2;
        demandedCursorHeightNorm = cursorHeightNorm / 1.5f;
        triangle = List.of(
                new VectorVertex(0.0f + demandedCursorWidthNorm / 2, 0.0f),
                new VectorVertex(0.0f - demandedCursorWidthNorm / 2, 0.0f + demandedCursorHeightNorm / 2),
                new VectorVertex(0.0f - demandedCursorWidthNorm / 2, 0.0f - demandedCursorHeightNorm / 2)
        );
        demandedMeterCursorShape = new VectorTriangle(triangle , vectorShader);
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isEmpty()) return;
        var droneStatus = drone.get().droneStatus;
        position = new Vector2f(droneStatus.position.x, droneStatus.position.y);
        climbRate = droneStatus.linearVelocity.z * Z_AXIS_INVERSION;
        height = droneStatus.position.z * Z_AXIS_INVERSION;
        controlModeDemanded = simulationState.getCurrentControlModeDemanded();

        updateDemanded(simulationState, droneStatus);
    }

    private void updateDemanded(SimulationState simulationState, DroneStatus drone) {
        if(
            simulationState.getCurrentControlModeDemanded() == null ||
            !simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.Z)
        )
            drawDemandedHeight = false;
        else {
            float demanded = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.Z);
            demandedHeight = -demanded;
            drawDemandedHeight = true;
        }

        if(simulationState.getCurrentControlModeDemanded() != null && simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.U)) {
            float demanded = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.U);
            var rotBW = new Matrix3f().rotate(drone.rotation);
            var velocityB = new Vector3f(drone.linearVelocity).mul(rotBW.transpose());
            velocity = velocityB.x;
            demandedVelocityX = demanded;
            drawDemandedVelocityX = true;
        } else {
            velocity = (float) Math.sqrt(drone.linearVelocity.x * drone.linearVelocity.x + drone.linearVelocity.y * drone.linearVelocity.y);
            drawDemandedVelocityX = false;
        }
    }

    public void draw(MemoryStack stack) {

        float meterBottomNorm = OPENGL_CANVAS_BOTTOM + (float) OPENGL_CANVAS_SIZE / 4;

        float velocityMeterLeftNorm = OPENGL_CANVAS_LEFT;
        drawMeter(stack, velocity, false, false, meterBottomNorm, velocityMeterLeftNorm, demandedVelocityX, drawDemandedVelocityX);

        float heightMeterLeftNorm = OPENGL_CANVAS_RIGHT - meterWidthNorm;
        drawMeter(stack, height, true, true, meterBottomNorm, heightMeterLeftNorm, demandedHeight, drawDemandedHeight);

        String modeString = controlModeDemanded != null? MessageFormat.format("{0}", controlModeDemanded.name): "";
        textEngine.setPosition(-0.95f, 5f/8);
        textEngine.renderText(modeString);
        String positionXString = MessageFormat.format("x: {0}", position.x);
        textEngine.setPosition(-0.95f, -3f/4);
        textEngine.renderText(positionXString);
        String positionYString = MessageFormat.format("y: {0}", position.y);
        textEngine.setPosition(-0.95f, -7f/8);
        textEngine.renderText(positionYString);
        String climbRateSTring = MessageFormat.format("cr: {0}", sigDigRounder(climbRate, 4,1));
        textEngine.setPosition(0.65f, -7f/8);
        textEngine.renderText(climbRateSTring);
    }

    private void drawMeter(
            MemoryStack stack,
            float meterValue,
            boolean allowNegative,
            boolean mirrored,
            float meterBottomNorm,
            float meterLeftNorm,
            float demandedValue,
            boolean drawDemandedValue
    ) {

        var bgTransform = new Matrix3x2f();
        bgTransform.translate(meterLeftNorm - (0.0f - meterWidthNorm / 2), 0);
        meterBackgroundShape.setTransform(bgTransform);
        meterBackgroundShape.draw(stack);

        var stripeTransform = new Matrix3x2f();
        stripeTransform.translate(meterLeftNorm - (0.0f - stripeWidthNorm / 2), 0);
        if(mirrored)
            stripeTransform.translate(meterWidthNorm - stripeWidthNorm, 0);
        meterStripeShape.setTransform(stripeTransform);
        meterStripeShape.draw(stack);

        var cursorTransform = new Matrix3x2f();
        cursorTransform.translate(meterLeftNorm - (0.0f - cursorWidthNorm / 2), 0);
        if(!mirrored){
            cursorTransform.translate(meterWidthNorm - cursorWidthNorm, 0);
            cursorTransform.rotate((float)Math.PI);
        }
        meterCursorShape.setTransform(cursorTransform);
        meterCursorShape.draw(stack);

        Vector4f cropRectangle = new Vector4f(meterLeftNorm, meterBottomNorm, meterWidthNorm, meterHeightNorm);
        if(drawDemandedValue) {
            var demandedCursorTransform = new Matrix3x2f();
            demandedCursorTransform.translate(meterLeftNorm - (0.0f - demandedCursorWidthNorm / 2), 0);
            if(!mirrored){
                demandedCursorTransform.translate(meterWidthNorm - demandedCursorWidthNorm, 0);
                demandedCursorTransform.rotate((float)Math.PI);
            }
            float deltaHeight = demandedValue - meterValue;
            float deltaDemanded = deltaHeight * METER_UNIT_HEIGHT / horizonScreenY * METER_PRECISION * OPENGL_CANVAS_SIZE;
            demandedCursorTransform.translate(0, deltaDemanded);
            demandedMeterCursorShape.setCropRectangle(cropRectangle);
            demandedMeterCursorShape.setColor(Color.RED);
            demandedMeterCursorShape.setTransform(demandedCursorTransform);
            demandedMeterCursorShape.draw(stack);
        }

        int pointCount = (int) (meterHeightNorm / (METER_UNIT_HEIGHT / horizonScreenY * OPENGL_CANVAS_SIZE) / METER_PRECISION) + 1;
        int numberCount = (int) (meterHeightNorm / (METER_UNIT_HEIGHT / horizonScreenY * OPENGL_CANVAS_SIZE) / NUMBER_PRECISION) + 1;

        float fraction = meterValue % METER_PRECISION;
        float topMeterValue = ((meterHeightNorm + (STRING_DRAWING_MARGIN / horizonScreenY * OPENGL_CANVAS_SIZE)) / ( (METER_UNIT_HEIGHT / horizonScreenY * OPENGL_CANVAS_SIZE) / METER_PRECISION)) / 2.f + meterValue;
        int topMeterNumber = (int) ((int) (topMeterValue / NUMBER_PRECISION) * NUMBER_PRECISION);
        int number = topMeterNumber;
        float numberFraction = topMeterValue - number;

        for(int i= 0; i < numberCount; i++) {
            textEngine.setCropRectangle(cropRectangle);
            textEngine.setPosition(meterLeftNorm + 0.075f, meterBottomNorm + 2*(STRING_DRAWING_MARGIN - i * METER_UNIT_HEIGHT / horizonScreenY * NUMBER_PRECISION - ((float) (int) (numberFraction * METER_UNIT_HEIGHT) / horizonScreenY) - FONT_SIZE_NORM));
            textEngine.renderText(String.valueOf(number));
            textEngine.setCropRectangle(new Vector4f(-1, -1, 2, 2));
            number -= (int) NUMBER_PRECISION;
            if(!allowNegative && number < 0)
                break;
        }
        meterBarsShape.setCropRectangle(cropRectangle);
        for(int i = 0; i < pointCount; i++) {
            var barTransform = new Matrix3x2f();
            barTransform.translate(meterLeftNorm - (0.0f - barWidthNorm / 2), 0);
            barTransform.translate(0, -(meterBottomNorm + i * (METER_UNIT_HEIGHT / horizonScreenY * OPENGL_CANVAS_SIZE) + (fraction * (METER_UNIT_HEIGHT / horizonScreenY * OPENGL_CANVAS_SIZE))));
            if(mirrored)
                barTransform.translate(meterWidthNorm - barWidthNorm, 0);
            meterBarsShape.setTransform(barTransform);
            meterBarsShape.draw(stack);
        }
    }
}
