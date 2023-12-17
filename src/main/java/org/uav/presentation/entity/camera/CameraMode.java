package org.uav.presentation.entity.camera;

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

    @Override
    public String toString() {
        return switch (this)
        {
            case DroneCamera -> "Drone Camera";
            case RacingCamera -> "Racing Camera";
            case HorizontalCamera -> "Horizontal Camera";
            case HardFPV -> "Hard FPV";
            case SoftFPV -> "Soft FPV";
            case ObserverCamera -> "Observer Camera";
            case FreeCamera -> "Free Camera";
        };
    }
}
