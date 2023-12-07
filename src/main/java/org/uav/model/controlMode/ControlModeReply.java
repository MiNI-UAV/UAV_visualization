package org.uav.model.controlMode;

import java.util.List;

public enum ControlModeReply {
    X,
    Y,
    Z,
    ROLL,
    PITCH,
    YAW,
    U,
    V,
    W,
    P,
    Q,
    R;

    public static final List<ControlModeReply> xMarkDemands = List.of(X, Y, Z, YAW);
}