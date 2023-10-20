package org.uav.scene.gui.widget.radar.layers;

import org.uav.scene.gui.DrawableGuiLayer;
import org.uav.scene.gui.widget.radar.RadarPoint;

import java.awt.*;
import java.util.List;

public class RadarPointsLayer implements DrawableGuiLayer {
    private final static int POINT_DIAMETER = 4;

    private final List<RadarPoint> radarPoints;
    private final int startingTraceStrength;
    private final float radarRange;
    private final int radarRadius;

    public RadarPointsLayer(List<RadarPoint> radarPoints, int startingTraceStrength, float radarRange, int radarPointsCanvasX) {
        this.radarPoints = radarPoints;
        this.startingTraceStrength = startingTraceStrength;
        this.radarRange = radarRange;
        radarRadius = radarPointsCanvasX / 2;
    }

    @Override
    public void draw(Graphics2D g) {
        radarPoints.forEach(point -> {
            float scale = radarRadius / radarRange;
            // X and Y adjusted
            int x = (int) (point.coordinates.y + radarRange * scale);
            int y = (int) (-point.coordinates.x + radarRange * scale);
            float strength = (float) point.traceStrength / startingTraceStrength;
            g.setColor(new Color(0, 1, 0, strength));
            g.fillOval(x, y, POINT_DIAMETER, POINT_DIAMETER);
        });
    }
}
