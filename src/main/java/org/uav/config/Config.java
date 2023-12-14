package org.uav.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.uav.input.CameraMode;

import javax.annotation.Nullable;
import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Config {

    BindingsConfig bindingsConfig;
    ServerSettings serverSettings;
    DroneSettings droneSettings;
    AudioSettings audioSettings;
    SceneSettings sceneSettings;
    GraphicsSettings graphicsSettings;
    MiscSettings miscSettings;
    Ports ports;

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BindingsConfig {
        Boolean generateOnStartUp;
        String source;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerSettings {
        String serverAddress;
        String assetsSourceUrl;
        Boolean downloadMissingAssets;
        @Nullable
        String assetsToUse;
        Integer heartBeatIntervalMs;
        Integer serverTimoutMs;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class DroneSettings {
        String droneName;
        String droneConfig;
        CameraMode defaultCamera;
        List<String> modes;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AudioSettings {
        Float soundVolume;
        Float musicVolume;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class SceneSettings {
        Boolean drawInWorldDemandedPositionalCoords;
        Float sunAngleDayCycle;
        Float sunAngleYearCycle;
        Float fogDensity;
        Float[] cameraFPP;
        Float[] cameraTPP;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GraphicsSettings {
        Integer windowWidth;
        Integer windowHeight;
        FullScreenMode fullScreenMode;
        Integer monitor;
        Float guiScale;
        Float fov;
        Boolean useGammaCorrection;
        Float gammaCorrection;
        Boolean useShadows;
        Integer shadowsTextureResolution;
        Integer shadowsRenderingDistance;
        Boolean showDebugInfo;
    }

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class MiscSettings {
        Boolean enableMusic;
        String musicDirectory;
        Boolean musicOnStartup;
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
