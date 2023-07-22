package org.uav.input;

import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.model.status.JoystickStatus;
import org.uav.processor.SimulationStateProcessor;
import org.uav.queue.Actions;
import org.uav.queue.ControlModes;
import org.uav.queue.JoystickProducer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static java.lang.Math.abs;
import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private final SimulationState simulationState;
    private final SimulationStateProcessor simulationStateProcessor;
    private final Config config;
    private final JoystickStatus joystickStatus;
    private final JoystickProducer joystickProducer;
    private boolean holdShoot = false;

    byte[] prevButtonsState = new byte[32];


    public InputHandler(SimulationStateProcessor simulationStateProcessor, SimulationState simulationState, Config config) {
        this.config = config;
        this.simulationState = simulationState;
        this.simulationStateProcessor = simulationStateProcessor;
        int joystickAxisCount = config.joystick.mappings.axes.size();
        joystickStatus = new JoystickStatus(joystickAxisCount);
        joystickProducer = new JoystickProducer();
    }

    public void handleInput(long window) {
        handleCameraModeChange(window);
        if(simulationState.getCurrentlyControlledDrone() != null)
            handleJoystick();
    }

    private void handleCameraModeChange(long window) {
        if (glfwGetKey(window, GLFW_KEY_0) == GLFW_PRESS)
            changeCameraMode(CameraMode.FreeCamera);
        if (glfwGetKey(window, GLFW_KEY_1) == GLFW_PRESS)
            changeCameraMode(CameraMode.DroneCamera);
        if (glfwGetKey(window, GLFW_KEY_2) == GLFW_PRESS)
            changeCameraMode(CameraMode.ObserverCamera);
        if (glfwGetKey(window, GLFW_KEY_3) == GLFW_PRESS)
            changeCameraMode(CameraMode.RacingCamera);
        if (glfwGetKey(window, GLFW_KEY_4) == GLFW_PRESS)
            changeCameraMode(CameraMode.HorizontalCamera);
        if (glfwGetKey(window, GLFW_KEY_5) == GLFW_PRESS)
            changeCameraMode(CameraMode.HardFPV);
        if (glfwGetKey(window, GLFW_KEY_6) == GLFW_PRESS)
            changeCameraMode(CameraMode.SoftFPV);
    }

    private void changeCameraMode(CameraMode cameraMode) {
        simulationState.setCurrentCameraMode(cameraMode);
        System.out.println("Camera mode: " + cameraMode);
    }

    private void handleJoystick() {
        int joystick = 0;
        while(!glfwJoystickPresent(joystick) && joystick < 10)
            joystick++;
        if(joystick == 10)
            System.out.println("[Error] No joystick found!");
        if(glfwJoystickPresent(joystick))
        {
            // debugPrintOutAxes(joystick);
            int count1 = 0;
            FloatBuffer floatBuffer = glfwGetJoystickAxes(joystick);

            while (floatBuffer != null && floatBuffer.hasRemaining()) {
                float axes = floatBuffer.get();
                if(config.joystick.mappings.axes.containsKey(count1))
                    joystickStatus.rawData[config.joystick.mappings.axes.get(count1).getRawDataArrayIndex()] = convertToRawData(count1, axes);
                if(config.joystick.mappings.axisActions.containsKey(count1))
                    handleAxis(config.joystick.mappings.axisActions.get(count1),axes);
                count1++;
            }

            ByteBuffer byteBuffer = glfwGetJoystickButtons(joystick);
            byte[] arr = new byte[byteBuffer.remaining()];
            byteBuffer.get(arr);
            handleButtons(arr);
            joystickProducer.send(simulationState.getCurrentlyControlledDrone(), joystickStatus);
        }
    }

    private void debugPrintOutAxes(int joystick) {
        int count = 0;
        FloatBuffer floatBuffer = glfwGetJoystickAxes(joystick);
        System.out.print("Axes:");
        while (floatBuffer != null && floatBuffer.hasRemaining()) {
            float axes = floatBuffer.get();
            System.out.print(count + "," + axes + " ");
            count++;
        }
        System.out.println();
    }

    private void handleButtons(byte[] buttonState) {

        for (int i = 0; i < buttonState.length; i++) {
            if(buttonState[i] == 0 || buttonState[i] == prevButtonsState[i]) continue;
            if(!config.joystick.mappings.buttonActions.containsKey(i)) continue;
            switch(config.joystick.mappings.buttonActions.getOrDefault(i,JoystickButtonFunctions.unused))
            {
                case respawn -> simulationStateProcessor.respawnDrone();
                case nextCamera -> simulationState.setCurrentCameraMode(simulationState.getCurrentCameraMode().next());
                case prevCamera ->  simulationState.setCurrentCameraMode(simulationState.getCurrentCameraMode().prev());
                case acroMode -> joystickProducer.send(simulationState.getCurrentlyControlledDrone(), ControlModes.acro);
                case angleMode -> joystickProducer.send(simulationState.getCurrentlyControlledDrone(), ControlModes.angle);
                case posMode -> joystickProducer.send(simulationState.getCurrentlyControlledDrone(), ControlModes.pos);
                case noneMode -> joystickProducer.send(simulationState.getCurrentlyControlledDrone(), ControlModes.none);
                case unused -> {}
            }
        }

        prevButtonsState = buttonState;
    }

    private void handleAxis(Actions action, float axisValue) {
        switch(action)
        {
            case shoot ->
            {
                if(!holdShoot && axisValue > 0.5 ) {
                    holdShoot = true;
                    joystickProducer.send(simulationState.getCurrentlyControlledDrone(), action);
                }
                if(holdShoot && axisValue < -0.5) {
                    holdShoot = false;
                }
            }
        }
    }

    private int convertToRawData(int index, float axes) {
        // axes is standardized to be in [-1,1]
        // Our standard requires [0,1024] and should take into account axis inversion
        Boolean inverted = config.joystick.mappings.axisInversions.get(index);
        return (int)((inverted? -1.0f: 1.0f) * deadZone(axes) * 512.0f + 512.0f);
    }

    private float deadZone(float axes) {
        return abs(axes) < config.joystick.deadZoneFactor ? 0.0f : axes;
    }
}
