package org.uav.scene.drawable.gui.widget.artificialHorizon.layers;

import org.joml.Vector2f;
import org.uav.model.SimulationState;
import org.uav.queue.ControlMode;
import org.uav.scene.drawable.gui.DrawableGuiLayer;

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
    private ControlMode mode;

    public ArtificialHorizonMetersLayer(
            int horizonScreenX,
            int horizonScreenY
    ) {
        this.horizonScreenX = horizonScreenX;
        this.horizonScreenY = horizonScreenY;
    }

    public void update(SimulationState simulationState) {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().id);
        if(drone == null) return;

        position = new Vector2f(drone.position.x, drone.position.y);
        velocity = (float) Math.sqrt(drone.linearVelocity.x * drone.linearVelocity.x + drone.linearVelocity.y * drone.linearVelocity.y);
        climbRate = drone.linearVelocity.z * Z_AXIS_INVERSION;
        height = drone.position.z * Z_AXIS_INVERSION;
        mode = simulationState.getCurrentControlMode();
    }
    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));

        drawMeter(g, velocity, false, false, horizonScreenY / 4, 0, horizonScreenX / 8, horizonScreenY / 2);
        drawMeter(g, height, true, true, horizonScreenY / 4, horizonScreenX * 7 / 8, horizonScreenX / 8, horizonScreenY / 2);

        String modeString = MessageFormat.format("{0}", mode);
        g.drawString(modeString, 10, horizonScreenY * 3 / 16);
        String positionXString = MessageFormat.format("x: {0}", position.x);
        g.drawString(positionXString, 10, horizonScreenY * 14 / 16);
        String positionYString = MessageFormat.format("y: {0}", position.y);
        g.drawString(positionYString, 10, horizonScreenY * 15 / 16);
        String climbRateSTring = MessageFormat.format("cr: {0}", sigDigRounder(climbRate, 4,1));
        g.drawString(climbRateSTring, horizonScreenX - 120, horizonScreenY * 15 / 16);
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
