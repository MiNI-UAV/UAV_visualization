package org.opengl.input;

import org.joml.Vector3f;
import org.opengl.config.Configuration;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class InputHandler {
    public Configuration configuration;


    public InputHandler(Configuration configuration) {
        this.configuration = configuration;
    }

    public void processInput(long window) {
        if (glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS)  // Front Camera
            configuration.type = CameraType.FrontCamera;
        if (glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS) // Jupiter Camera
            configuration.type = CameraType.JupiterCamera;
        if (glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS)  // Gabriel Camera
            configuration.type = CameraType.GabrielCamera;

        if (glfwGetKey(window, GLFW_KEY_4) == GLFW_PRESS)  // Phong Shader
            configuration.shader = configuration.phongShader;
        if (glfwGetKey(window, GLFW_KEY_5) == GLFW_PRESS) // Gouraud Shader
            configuration.shader = configuration.gouraudShader;
        if (glfwGetKey(window, GLFW_KEY_6) == GLFW_PRESS)  // Flat Shader
            configuration.shader = configuration.flatShader;
        configuration.shader.use();

        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS)  // Toggle Night
            configuration.isDay = false;
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)  // Toggle Day
            configuration.isDay = true;

        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)  // Toggle Fog Off
            configuration.useFog = false;
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)  // Toggle Fog On
            configuration.useFog = true;

        if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS)  // Fog Density Down
            configuration.fogDensity = configuration.fogDensity / 1.02f;
        if (glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS)  // Fog Density Up
            configuration.fogDensity = configuration.fogDensity * 1.02f;

        if (glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS)  // Jupiter Shake Off
            configuration.shakeFactor = 0f;
        if (glfwGetKey(window, GLFW_KEY_V) == GLFW_PRESS)  // Jupiter Shake On
            configuration.shakeFactor = 0.05f;

        if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS)  // Move Red Spotlight
            configuration.currentSpotlight = SpotlightType.RedSpotlight;
        if (glfwGetKey(window, GLFW_KEY_G) == GLFW_PRESS)  // Move Green Spotlight
            configuration.currentSpotlight = SpotlightType.GreenSpotlight;
        if (glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS)  // Move Blue Spotlight
            configuration.currentSpotlight = SpotlightType.BlueSpotlight;

        if(configuration.currentSpotlight == SpotlightType.BlueSpotlight) {
            if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS)  // Spotlight Up
                configuration.blueVector.add(new Vector3f(0f, 0.05f, 0f));
            if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS)  // Spotlight Down
                configuration.blueVector.add(new Vector3f(0f, -0.05f, 0f));
            if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS)  // Spotlight Left
                configuration.blueVector.add(new Vector3f(-0.05f, 0f, -0.05f));
            if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS)  // Spotlight Right
                configuration.blueVector.add(new Vector3f(0.05f, 0f, 0.05f));
        }

        if(configuration.currentSpotlight == SpotlightType.GreenSpotlight) {
            if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS)  // Spotlight Up
                configuration.greenVector.add(new Vector3f(0f, 0.05f, 0f));
            if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS)  // Spotlight Down
                configuration.greenVector.add(new Vector3f(0f, -0.05f, 0f));
            if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS)  // Spotlight Left
                configuration.greenVector.add(new Vector3f(-0.05f, 0f, -0.05f));
            if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS)  // Spotlight Right
                configuration.greenVector.add(new Vector3f(0.05f, 0f, 0.05f));
        }

        if(configuration.currentSpotlight == SpotlightType.RedSpotlight) {
            if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS)  // Spotlight Up
                configuration.redVector.add(new Vector3f(0f, 0.05f, 0f));
            if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS)  // Spotlight Down
                configuration.redVector.add(new Vector3f(0f, -0.05f, 0f));
            if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS)  // Spotlight Left
                configuration.redVector.add(new Vector3f(-0.05f, 0f, -0.05f));
            if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS)  // Spotlight Right
                configuration.redVector.add(new Vector3f(0.05f, 0f, 0.05f));
        }
    }
}
