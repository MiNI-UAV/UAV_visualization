package org.uav.presentation.entity.gui.widget.artificialHorizon.layers;

import org.joml.Matrix3f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.uav.logic.state.controlMode.ControlModeDemanded;
import org.uav.logic.state.controlMode.ControlModeReply;
import org.uav.logic.state.drone.DroneStatus;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.DrawableGuiLayer;

import java.awt.*;
import java.text.MessageFormat;

import static org.uav.utils.SignificantDigitsRounder.sigDigRounder;

public class ArtificialHorizonMetersLayer implements DrawableGuiLayer {
    private static final int Z_AXIS_INVERSION = -1;
    private static final Color BG_COLOR = new Color(0, 0, 0, 32);
    private static final int FONT_SIZE = 18;
    private static final int STRING_DRAWING_MARGIN = (FONT_SIZE + 1) / 2;
    private static final int METER_UNIT_HEIGHT = 15;
    private static final int METER_PRECISION = 1;
    private static final int NUMBER_PRECISION = 2 * METER_PRECISION;
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

    public ArtificialHorizonMetersLayer(
            int horizonScreenX,
            int horizonScreenY
    ) {
        this.horizonScreenX = horizonScreenX;
        this.horizonScreenY = horizonScreenY;
        position = new Vector2f();
        velocity = 0;
        climbRate = 0;
        height = 0;
        drawDemandedHeight = false;
        drawDemandedVelocityX = false;
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
    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));

        int meterTop = horizonScreenY / 4;
        int meterHeight = horizonScreenY / 2;
        int meterWidth = horizonScreenX / 8;

        int velocityMeterLeft = 0;
        int velocityMeterRight = velocityMeterLeft + meterWidth;
        drawMeter(g, velocity, false, false, meterTop, velocityMeterLeft, meterWidth, meterHeight);
        drawDemanded(g, false, velocityMeterLeft, meterTop, meterWidth , meterHeight, velocityMeterRight, velocity, demandedVelocityX, drawDemandedVelocityX);

        int heightMeterLeft = horizonScreenX * 7 / 8;
        int heightMeterRight = heightMeterLeft + meterWidth;
        drawMeter(g, height, true, true, meterTop, heightMeterLeft, meterWidth, meterHeight);
        drawDemanded(g, true, heightMeterLeft, meterTop, meterWidth, meterHeight, heightMeterRight, height, demandedHeight, drawDemandedHeight);

        g.setColor(Color.white);
        String modeString = controlModeDemanded != null? MessageFormat.format("{0}", controlModeDemanded.name): "";
        g.drawString(modeString, 10, horizonScreenY * 3 / 16);
        String positionXString = MessageFormat.format("x: {0}", position.x);
        g.drawString(positionXString, 10, horizonScreenY * 14 / 16);
        String positionYString = MessageFormat.format("y: {0}", position.y);
        g.drawString(positionYString, 10, horizonScreenY * 15 / 16);
        String climbRateSTring = MessageFormat.format("cr: {0}", sigDigRounder(climbRate, 4,1));
        g.drawString(climbRateSTring, horizonScreenX - 120, horizonScreenY * 15 / 16);
    }

    private void drawDemanded(Graphics2D g, boolean inverted, int meterLeft, int meterTop, int meterWidth, int meterHeight, int meterRight, float value, float demandedValue, boolean drawDemandedValue) {
        if(drawDemandedValue) {
            Shape meterBg = new Rectangle(meterLeft, meterTop, meterWidth, meterHeight);
            g.setClip(meterBg);
            g.setColor(new Color(1,0,0,0.5f));
            g.setStroke(new BasicStroke(3));
            float deltaHeight = demandedValue - value;
            float deltaDemanded = deltaHeight * METER_UNIT_HEIGHT * METER_PRECISION;
            g.drawLine(
                    inverted? meterRight - 20: meterLeft,
                    meterTop + meterHeight / 2 - (int) deltaDemanded,
                    inverted ? meterRight: meterLeft + 20,
                    meterTop + meterHeight / 2 - (int) deltaDemanded
            );
            g.setStroke(new BasicStroke());
            g.setClip(null);
        }
    }

    private void drawMeter(
            Graphics2D g,
            float meterValue,
            boolean allowNegative,
            boolean mirrored,
            int meterTop,
            int meterLeft,
            int meterWidth,
            int meterHeight
    ) {
        int meterRight = meterLeft + meterWidth;
        int meterCenter = meterTop + meterHeight / 2;

        g.setColor(BG_COLOR);
        Shape meterBg = new Rectangle(meterLeft, meterTop, meterWidth, meterHeight);
        g.setClip(meterBg);
        g.fill(meterBg);
        g.setColor(Color.white);
        if(mirrored)
            g.fillRect(meterRight - 2, meterTop, 2, meterHeight);
        else
            g.fillRect(meterLeft, meterTop, 2, meterHeight);

        var triangle = mirrored ?
                new Polygon(
                        new int[] {meterLeft, meterLeft, meterLeft + meterWidth / 3},
                        new int[] {meterCenter + METER_UNIT_HEIGHT / 4, meterCenter - METER_UNIT_HEIGHT / 4, meterCenter},
                        3
        ):
                new Polygon(
                        new int[] {meterRight, meterRight, meterRight - meterWidth / 3},
                        new int[] {meterCenter - METER_UNIT_HEIGHT / 4, meterCenter + METER_UNIT_HEIGHT / 4, meterCenter},
                        3
        )
                ;
        g.fillPolygon(triangle);

        int pointCount = meterHeight / METER_UNIT_HEIGHT / METER_PRECISION + 1;
        int numberCount = meterHeight / METER_UNIT_HEIGHT / NUMBER_PRECISION + 1;

        float fraction = meterValue % METER_PRECISION;
        float topMeterValue = ((float) (meterHeight + STRING_DRAWING_MARGIN) / ((float) METER_UNIT_HEIGHT / METER_PRECISION)) / 2.f + meterValue;
        int topMeterNumber = (int) topMeterValue / NUMBER_PRECISION * NUMBER_PRECISION;
        int number = topMeterNumber;
        float numberFraction = topMeterValue - number;

        for(int i= 0; i < numberCount; i++) {
            g.drawString(
                    String.valueOf(number),
                    meterLeft + 8,
                    meterTop - STRING_DRAWING_MARGIN + i * METER_UNIT_HEIGHT * NUMBER_PRECISION + (int) (numberFraction * METER_UNIT_HEIGHT) + FONT_SIZE / 2
            );
            number -= NUMBER_PRECISION;
            if(!allowNegative && number < 0)
                break;
        }
        for(int i= 0; i < pointCount; i++) {
            if(mirrored)
                g.fillRect(meterRight - 8, meterTop + i * METER_UNIT_HEIGHT + (int) (fraction * METER_UNIT_HEIGHT) - 1, 8, 2);
            else
                g.fillRect(meterLeft, meterTop + i * METER_UNIT_HEIGHT + (int) (fraction * METER_UNIT_HEIGHT) - 1, 8, 2);
        }
        g.setClip(null);
    }
}