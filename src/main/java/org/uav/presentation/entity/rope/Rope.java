package org.uav.presentation.entity.rope;

import org.joml.Vector3f;

public class Rope {
    public static int SEGMENT_COUNT = 10;
    public static float THICKNESS = 0.03f;
    public static final Vector3f ROPE_COLOR_1 = new Vector3f(228f / 256, 208f / 256, 155f / 256);
    public static final Vector3f ROPE_COLOR_2 = new Vector3f(131f / 256, 89f / 256, 26f / 256);

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
