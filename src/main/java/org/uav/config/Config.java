package org.uav.config;

import org.uav.UavVisualization;
import org.uav.input.CameraMode;
import org.uav.input.JoystickButtonFunctions;
import org.uav.queue.Actions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Config {

    public int windowWidth;
    public int windowHeight;
    public String serverAddress;
    public String droneName;
    public CameraMode defaultCamera;
    public JoystickConfig joystick;
    public Ports ports;

    public static class JoystickConfig {
        public float deadZoneFactor;
        public JoystickMappings mappings;

        public static class JoystickMappings {

            public Map<Integer, JoystickButtonFunctions> buttonActions;
            public Map<Integer, Actions> axisActions;
            public Map<Integer, Integer> axes;
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
            var source = new Yaml(new Constructor(Config.class, new LoaderOptions()));
            return source.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
