package org.uav.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.uav.input.CameraMode;
import org.uav.input.JoystickButtonFunctions;
import org.uav.model.DroneMovement;
import org.uav.queue.Actions;

import java.util.List;
import java.util.Map;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Config {

    String bindingsConfig;
    ServerSettings serverSettings;
    DroneSettings droneSettings;
    SceneSettings sceneSettings;
    GraphicsSettings graphicsSettings;
    JoystickConfig joystick;
    Ports ports;

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerSettings {
        String serverAddress;
        String assetsSourceUrl;
        boolean downloadMissingAssets;
        String assetsToUse;
        int heartBeatIntervalMs;
        int serverTimoutMs;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class DroneSettings {
        String droneName;
        String droneModel;
        CameraMode defaultCamera;
        List<String> modes;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class SceneSettings {
        boolean drawInWorldDemandedPositionalCoords;
        float sunAngleDayCycle;
        float sunAngleYearCycle;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GraphicsSettings {
        int windowWidth;
        int windowHeight;
        FullScreenMode fullScreenMode;
        Integer monitor;
        float guiScale;
        float fov;
        boolean useGammaCorrection;
        float gammaCorrection;
        boolean useShadows;
        int shadowsTextureResolution;
        int shadowsRenderingDistance;
    }
    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class JoystickConfig {
        float deadZoneFactor;
        JoystickMappings mappings;

        @Value
        @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
        public static class JoystickMappings {
            Map<Integer, JoystickButtonFunctions> buttonActions;
            Map<Integer, Actions> axisActions;
            Map<Integer, DroneMovement> axes;
            Map<Integer, Boolean> axisInversions;
        }
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class Ports {
        int notifications;
        int droneRequester;
        int droneStatuses;
        int projectileStatuses;
    }
}
