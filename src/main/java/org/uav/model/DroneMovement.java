package org.uav.model;

public enum DroneMovement {
    rotation(0),
    heightChange(1),
    sidewaysMovement(2),
    straightMovement(3);

    private final int rawDataArrayIndex;

    private DroneMovement(int rawDataArrayIndex) {
        this.rawDataArrayIndex = rawDataArrayIndex;
    }

    public int getRawDataArrayIndex() {
        return rawDataArrayIndex;
    }
}
