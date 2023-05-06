package org.uav.input;

import org.uav.config.Configuration;
import org.uav.status.JoystickStatus;
import org.uav.queue.JoystickProducer;
import org.zeromq.ZContext;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    public Configuration configuration;
    private final JoystickStatus joystickStatus;
    private final JoystickProducer joystickProducer;


    public InputHandler(Configuration configuration, ZContext context) {
        this.configuration = configuration;
        int joystickAxisCount = 4;
        joystickStatus = new JoystickStatus(joystickAxisCount);
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

            while (floatBuffer != null && floatBuffer.hasRemaining()) {
                float axes = floatBuffer.get();
                //System.out.print(count1 + "," + axes + " ");
                if(configuration.joystickMapping.containsKey(count1))
                    joystickStatus.rawData[configuration.joystickMapping.get(count1)] = convertToRawData(count1, axes);
                count1++;
            }

            int count2 = 0;
            //System.out.print("Button:");
            ByteBuffer byteBuffer = glfwGetJoystickButtons(joystick);
            while (byteBuffer != null && byteBuffer.hasRemaining()) {
                byte button = byteBuffer.get();
                //System.out.print(count2 + "," + button + " ");
                //if(configuration.joystickMapping.containsKey(count2))
                //    joystickStatus.rawData[configuration.joystickMapping.get(count1)] = convertToRawData(axes);
                count2++;
            }
            //System.out.println();
            joystickProducer.send(joystickStatus);
        }
    }

    private int convertToRawData(int index, float axes) {
        // axes is standardized to be in [-1,1]
        // Our standard requires [0,1024] and should take into account axis inversion
        Boolean inverted = configuration.joystickInversionMapping.get(index);
        return (int)((inverted? -1: 1) * axes * 512 + 512);
    }
}
