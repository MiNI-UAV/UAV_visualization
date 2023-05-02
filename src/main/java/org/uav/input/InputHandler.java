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

            while (floatBuffer != null && floatBuffer.hasRemaining()) {
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
            while (byteBuffer != null && byteBuffer.hasRemaining()) {
                byte button = byteBuffer.get();
                //System.out.print(count2 + "," + button + " ");
                if(count2 == 8 && button == 1)
                    joystickStatus.z -= 0.1;
                if(count2 == 9 && button == 1)
                    joystickStatus.z += 0.1;
                if(count2 == 10 && button == 1)
                    joystickStatus.yaw += 0.01;
                if(count2 == 11 && button == 1)
                    joystickStatus.yaw -= 0.01;
                count2++;
            }
            //System.out.println();
            joystickProducer.send(joystickStatus);
        }
    }
}
