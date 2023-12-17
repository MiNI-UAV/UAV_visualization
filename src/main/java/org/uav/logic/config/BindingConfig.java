package org.uav.logic.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BindingConfig {

    List<SteeringAxis> steering;
    Actions actions;
    List<List<Binding>> modes;

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @AllArgsConstructor
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Actions {
        List<Binding> shoot;
        List<Binding> drop;
        List<Binding> release;
        List<Binding> respawn;
        List<Binding> nextAmmo;
        List<Binding> nextCargo;
        List<Binding> map;
        List<Binding> toggleRadio;
        List<Binding> nextSong;
        List<Binding> toggleSpotLight;
        List<Binding> mapZoomIn;
        List<Binding> mapZoomOut;
        List<Binding> prevCamera;
        List<Binding> nextCamera;
        List<Binding> freeCamera;
        List<Binding> droneCamera;
        List<Binding> observerCamera;
        List<Binding> racingCamera;
        List<Binding> horizontalCamera;
        List<Binding> hardFPV;
        List<Binding> softFPV;

        public Actions() {
            shoot = new ArrayList<>();
            drop = new ArrayList<>();
            release = new ArrayList<>();
            respawn = new ArrayList<>();
            nextAmmo = new ArrayList<>();
            nextCargo = new ArrayList<>();
            map = new ArrayList<>();
            mapZoomIn = new ArrayList<>();
            mapZoomOut = new ArrayList<>();
            toggleRadio = new ArrayList<>();
            nextSong = new ArrayList<>();
            prevCamera = new ArrayList<>();
            nextCamera = new ArrayList<>();
            freeCamera = new ArrayList<>();
            droneCamera = new ArrayList<>();
            observerCamera = new ArrayList<>();
            racingCamera = new ArrayList<>();
            horizontalCamera = new ArrayList<>();
            hardFPV = new ArrayList<>();
            softFPV = new ArrayList<>();
            toggleSpotLight = new ArrayList<>();
        }
    }
    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Binding {
        //// Gamepad Axis
        Integer axis;
        Float lowerBound;
        Float upperBound;
        //// Gamepad Button
        Integer button;
        Boolean inverse;
        //// Keyboard Key
        Integer key;
    }
}
