 package org.uav.config;

import org.uav.input.CameraType;
import org.uav.input.JoystickButtonFunctions;
import org.uav.shader.Shader;

import java.util.Map;

public class Configuration {
    public Map<Integer, Integer> joystickMapping;
    public Map<Integer, Boolean> joystickInversionMapping;
    public Map<Integer, JoystickButtonFunctions> joystickButtonsMapping;
    //public String address = "127.0.0.1";
    public String address = "83.6.113.220";
    public String droneName = "Jacek";
    public float deadZoneFactor = 0.2f;
    public CameraType type = CameraType.HorizontalCamera;
    public boolean isDay = true;
    public float fogDensity = 0.03f;
    public boolean useFog = false;
    public Shader shader;
    public Shader phongShader;
    public Shader gouraudShader;
    public Shader flatShader;
}
