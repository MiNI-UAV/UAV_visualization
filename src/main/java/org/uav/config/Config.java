package org.uav.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.uav.input.CameraMode;
import org.uav.input.JoystickButtonFunctions;
import org.uav.model.DroneMovement;
import org.uav.queue.Actions;
import org.uav.queue.ControlMode;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Config {

    public String serverAddress;
    public int windowWidth;
    public int windowHeight;
    public float guiScale;
    public float fov;
    public String droneName;
    public String droneModel;
    public CameraMode defaultCamera;
    public ControlMode defaultControlMode;
    public boolean drawInWorldDemandedPositionalCoords;
    public JoystickConfig joystick;
    public Ports ports;
    public int heartBeatIntervalMs;

    public static class JoystickConfig {
        public float deadZoneFactor;
        public JoystickMappings mappings;

        public static class JoystickMappings {

            public Map<Integer, JoystickButtonFunctions> buttonActions;
            public Map<Integer, Actions> axisActions;
            public Map<Integer, DroneMovement> axes;
            public Map<Integer, Boolean> axisInversions;
        }
    }

    public static class Ports {
        public int notifications;
        public int droneRequester;
        public int droneStatuses;
        public int projectileStatuses;
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
