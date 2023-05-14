package org.uav.input;

public enum CameraType {
    DroneCamera,
    FreeCamera,
    RacingCamera,
    ObserverCamera,
    HorizontalCamera;

    private static final CameraType[] vals = values();

    public CameraType next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
    public CameraType prev() {
        return vals[(ordinal() - 1  + vals.length) % vals.length];
    }
}
