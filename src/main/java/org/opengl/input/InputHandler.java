package org.opengl.input;

import org.opengl.config.Configuration;
import org.opengl.model.JoystickStatus;
import org.opengl.queue.JoystickProducer;
import org.zeromq.ZContext;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    public Configuration configuration;
    private final JoystickStatus joystickStatus;
    private JoystickProducer joystickProducer;


    public InputHandler(Configuration configuration, ZContext context) {
        this.configuration = configuration;
        joystickStatus = new JoystickStatus();
        joystickProducer = new JoystickProducer(context);
    }

    public void processInput(long window) {
        // Camera Modes
        if (glfwGetKey(window, GLFW_KEY_0) == GLFW_PRESS)
            configuration.type = CameraType.FreeCamera;
        if (glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS)
            configuration.type = CameraType.DroneCamera;

        int joystick = 0;
        while(!glfwJoystickPresent(joystick) && joystick < 10)
            joystick++;
        if(joystick == 10)
            System.out.println("[Error] No joystick found!");
        if(glfwJoystickPresent(joystick))
        {
            int count1 = 0;
            FloatBuffer floatBuffer = glfwGetJoystickAxes(joystick);
            //System.out.print("Axes:");
            while (floatBuffer.hasRemaining()) {
                float axes = floatBuffer.get();
                //System.out.print(count1 + "," + axes + " ");
                if(count1 == 0 )
                    joystickStatus.roll = axes*2;
                if(count1 == 3 )
                    joystickStatus.pitch = -axes;
                count1++;
            }

            int count2 = 0;
            //System.out.print("Button:");
            ByteBuffer byteBuffer = glfwGetJoystickButtons(joystick);
            while (byteBuffer.hasRemaining()) {
                byte button = byteBuffer.get();
                //System.out.print(count2 + "," + button + " ");
                if(count2 == 8 && button == 1)
                    joystickStatus.z += -0.1;
                if(count2 == 9 && button == 1)
                    joystickStatus.z += 0.1;
                if(count2 == 10 && button == 1)
                    joystickStatus.yaw += 0.01;
                if(count2 == 11 && button == 1)
                    joystickStatus.yaw += -0.01;
                count2++;
            }
            //System.out.println();
            joystickProducer.send(joystickStatus);
        }
        //// Shader Types
        /*
        if (glfwGetKey(window, GLFW_KEY_4) == GLFW_PRESS)  // Phong Shader
            configuration.shader = configuration.phongShader;
        if (glfwGetKey(window, GLFW_KEY_5) == GLFW_PRESS) // Gouraud Shader
            configuration.shader = configuration.gouraudShader;
        if (glfwGetKey(window, GLFW_KEY_6) == GLFW_PRESS)  // Flat Shader
            configuration.shader = configuration.flatShader;
        configuration.shader.use();
        */

        /*if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS)  // Toggle Night
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
        }*/
    }
}
