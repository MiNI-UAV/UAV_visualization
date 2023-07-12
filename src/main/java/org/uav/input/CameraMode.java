package org.uav.input;

public enum CameraMode {
    DroneCamera,
    RacingCamera,
    HorizontalCamera,
    HardFPV,
    SoftFPV,
    ObserverCamera,
    FreeCamera;



    private static final CameraMode[] vals = values();

    public CameraMode next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
    public CameraMode prev() {
        return vals[(ordinal() - 1  + vals.length) % vals.length];
    }
}
