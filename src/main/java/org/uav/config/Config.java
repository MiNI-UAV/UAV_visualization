package org.uav.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.uav.input.CameraMode;
import org.uav.input.JoystickButtonFunctions;
import org.uav.model.DroneMovement;
import org.uav.queue.Actions;
import org.uav.queue.ControlMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Config {

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
        ControlMode defaultControlMode;
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
    public static Config load(Path path) {
        try {
            ObjectMapper mapper = new YAMLMapper();
            return mapper.readValue(new File(System.getProperty("user.dir"), path.toString()), Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
