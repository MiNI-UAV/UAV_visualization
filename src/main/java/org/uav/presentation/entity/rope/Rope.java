package org.uav.presentation.entity.rope;

import org.joml.Vector3f;

public class Rope {
    public static int SEGMENT_COUNT = 15;
    public static float THICKNESS = 0.015f;
    public static final Vector3f ROPE_COLOR_1 = new Vector3f(150f / 256, 57f / 256, 36f / 256);
    public static final Vector3f ROPE_COLOR_2 = new Vector3f(100f / 256, 44f / 256, 13f / 256);

    public final float ropeLength;
    public final int ownerId;
    public final int objectId;
    public final Vector3f ownerOffset;


    public Rope(float ropeLength, int ownerId, int objectId, Vector3f ownerOffset) {
        this.ropeLength = ropeLength;
        this.ownerId = ownerId;
        this.objectId = objectId;
        this.ownerOffset = ownerOffset;
    }
}
