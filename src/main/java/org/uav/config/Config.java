package org.uav.config;

import org.uav.UavVisualization;
import org.uav.input.CameraMode;
import org.uav.input.JoystickButtonFunctions;
import org.uav.model.DroneMovement;
import org.uav.queue.Actions;
import org.uav.queue.ControlMode;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Config {

    public String serverAddress;
    public int windowWidth;
    public int windowHeight;
    public float guiScale;
    public float fov;
    public String droneName;
    public String droneModel;
    public String map;
    public CameraMode defaultCamera;
    public ControlMode defaultControlMode;
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
        public int droneRequester;
        public int droneStatuses;
        public int projectileStatuses;
    }
    public static Config loadConfig(String path) {
        try (InputStream inputStream = UavVisualization.class
                .getClassLoader().getResourceAsStream(path)) {
            var constructor = new Constructor(Config.class, new LoaderOptions());
            var yaml = new Yaml(constructor);
            // yaml.setBeanAccess(BeanAccess.FIELD); TODO: Privatize config
            return yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
