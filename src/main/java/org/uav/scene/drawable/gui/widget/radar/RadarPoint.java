package org.uav.scene.drawable.gui.widget.radar;

import org.joml.Vector2f;

public class RadarPoint {
    public Vector2f coordinates;
    public int traceStrength;

    public RadarPoint(Vector2f coordinates, int traceStrength) {
        this.coordinates = coordinates;
        this.traceStrength = traceStrength;
    }
}
