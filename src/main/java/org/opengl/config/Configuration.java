package org.opengl.config;

import org.joml.Vector3f;
import org.opengl.input.CameraType;
import org.opengl.input.SpotlightType;
import org.opengl.shader.Shader;

public class Configuration {
    public CameraType type = CameraType.FreeCamera;
    public boolean isDay = true;
    public float fogDensity = 0.03f;
    public boolean useFog = false;
    public SpotlightType currentSpotlight = SpotlightType.BlueSpotlight;
    public Vector3f blueVector = new Vector3f(0f,-0.2f,0f);
    public Vector3f greenVector = new Vector3f(0f,0.2f,0f);
    public Vector3f redVector = new Vector3f(-0.3f, 0,-0.3f);
    public Shader shader;
    public Shader phongShader;
    public Shader gouraudShader;
    public Shader flatShader;
    public float shakeFactor;
}
