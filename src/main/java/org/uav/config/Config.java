package org.uav.config;

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
import java.util.Map;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Config {

    String serverAddress;
    int windowWidth;
    int windowHeight;
    FullScreenMode fullScreenMode;
    float guiScale;
    float fov;
    String droneName;
    String droneModel;
    CameraMode defaultCamera;
    ControlMode defaultControlMode;
    boolean drawInWorldDemandedPositionalCoords;
    JoystickConfig joystick;
    Ports ports;
    int heartBeatIntervalMs;
    int serverTimoutMs;

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
    public static Config load(String path) {
        try {
            ObjectMapper mapper = new YAMLMapper();
            return mapper.readValue(new File(System.getProperty("user.dir") + "/" + path), Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
