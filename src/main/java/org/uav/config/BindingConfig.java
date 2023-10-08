package org.uav.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class BindingConfig {

    List<SteeringAxis> steering;
    Actions actions;

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SteeringAxis {
        Integer axis;
        Boolean inverse;
        Float deadzone;
        Float min;
        Float max;
        Float trim;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Actions {
        List<Binding> shot;
        List<Binding> drop;
        List<Binding> respawn;
        List<Binding> map;
        List<Binding> mapZoomIn;
        List<Binding> mapZoomOut;
        List<List<Binding>> modes;
        List<Binding> prevCamera;
        List<Binding> nextCamera;
        List<Binding> freeCamera;
        List<Binding> droneCamera;
        List<Binding> observerCamera;
        List<Binding> racingCamera;
        List<Binding> horizontalCamera;
        List<Binding> hardFPV;
        List<Binding> softFPV;

        @Value
        @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Binding {
            //// Gamepad Axis
            Integer axis;
            Boolean inverse;
            Float lowerBound;
            Float upperBound;
            //// Gamepad Button
            Integer button;
            // boolean inverse;
            //// Keyboard Key
            Character key;
        }
    }
}
