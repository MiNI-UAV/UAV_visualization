package org.uav.config;

import org.uav.input.CameraType;
import org.uav.shader.Shader;

import java.util.Map;

public class Configuration {
    public Map<Integer, Integer> joystickMapping;
    public Map<Integer, Boolean> joystickInversionMapping;
    public CameraType type = CameraType.FreeCamera;
    public boolean isDay = true;
    public float fogDensity = 0.03f;
    public boolean useFog = false;
    public Shader shader;
    public Shader phongShader;
    public Shader gouraudShader;
    public Shader flatShader;
}
