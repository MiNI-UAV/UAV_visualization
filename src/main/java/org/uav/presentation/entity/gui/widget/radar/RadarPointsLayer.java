package org.uav.presentation.entity.gui.widget.radar;


import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryStack;
import org.uav.presentation.entity.vector.VectorCircle;
import org.uav.presentation.rendering.Shader;

import java.awt.*;
import java.util.List;

public class RadarPointsLayer {
    private final static int POINT_RADIUS = 2;
    private final static int POINT_CORNERS = 7;

    private final List<RadarPoint> radarPoints;
    private final int startingTraceStrength;
    private final float radarRange;
    private final int radarRadius;
    private final VectorCircle pointShape;

    public RadarPointsLayer(List<RadarPoint> radarPoints, int startingTraceStrength, float radarRange, int radarPointsCanvasX, Shader vectorShader) {
        this.radarPoints = radarPoints;
        this.startingTraceStrength = startingTraceStrength;
        this.radarRange = radarRange;
        radarRadius = radarPointsCanvasX / 2;
        pointShape = new VectorCircle(new Vector2f(), (float) POINT_RADIUS / radarPointsCanvasX, POINT_CORNERS, vectorShader);
    }

    public void draw(MemoryStack stack) {
        radarPoints.forEach(point -> {
            // X and Y adjusted
            float x = point.coordinates.x / radarRange;
            float y = -point.coordinates.y / radarRange;
            float strength = (float) point.traceStrength / startingTraceStrength;
            var transform = new Matrix3x2f();
            transform.translate(x, y);
            pointShape.setTransform(transform);
            pointShape.setColor(new Color(0, 1, 0, strength));
            pointShape.draw(stack);
        });
    }
}
