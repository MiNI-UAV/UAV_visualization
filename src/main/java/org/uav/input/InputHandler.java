package org.uav.input;

import org.uav.config.BindingConfig;
import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.model.status.JoystickStatus;
import org.uav.processor.SimulationStateProcessor;
import org.uav.queue.Actions;
import org.uav.queue.JoystickProducer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {

    private final SimulationState simulationState;
    private final List<BindingConfig.SteeringAxis> steeringAxisBindings;
    private final List<AxisBinding> axisBindings;
    private final List<ButtonBinding> buttonBindings;
    private final List<KeyboardBinding> keyboardBindings;
    private final SimulationStateProcessor simulationStateProcessor;
    private final Config config;
    private final JoystickProducer joystickProducer;


    public InputHandler(SimulationStateProcessor simulationStateProcessor, SimulationState simulationState, Config config, BindingConfig bindingConfig) throws IOException {
        this.config = config;
        this.simulationState = simulationState;
        this.simulationStateProcessor = simulationStateProcessor;
        joystickProducer = new JoystickProducer();
        axisBindings = new ArrayList<>();
        buttonBindings = new ArrayList<>();
        keyboardBindings = new ArrayList<>();
        steeringAxisBindings = new ArrayList<>();
        initBindings(bindingConfig);
    }

    private void initBindings(BindingConfig bindingConfig) throws IOException {
        // TODO better map overlay handling
        // TODO fix propeller display
        // Steering
        steeringAxisBindings.addAll(bindingConfig.getSteering());
        // Actions
        var actions = bindingConfig.getActions();
        for(int i=0; i<actions.getModes().size(); i++) {
            var modeId = i;
            var mode = actions.getModes().get(modeId);
            initAction(mode, () -> {if(!simulationState.isMapOverlay()) changeControlMode(config.getDroneSettings().getModes().get(modeId));});
        }
        initAction(actions.getShot(), () -> {if(!simulationState.isMapOverlay()) joystickProducer.send(simulationState.getCurrentlyControlledDrone(), Actions.shot);});
        initAction(actions.getDrop(), () ->{if(!simulationState.isMapOverlay()) joystickProducer.send(simulationState.getCurrentlyControlledDrone(), Actions.drop);});
        initAction(actions.getPrevCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(simulationState.getCurrentCameraMode().prev());});
        initAction(actions.getNextCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(simulationState.getCurrentCameraMode().next());});
        initAction(actions.getRespawn(), () -> {if(!simulationState.isMapOverlay()) simulationStateProcessor.respawnDrone();});
        initAction(actions.getFreeCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(CameraMode.FreeCamera);});
        initAction(actions.getDroneCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(CameraMode.DroneCamera);});
        initAction(actions.getObserverCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(CameraMode.ObserverCamera);});
        initAction(actions.getRacingCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(CameraMode.RacingCamera);});
        initAction(actions.getHorizontalCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(CameraMode.HorizontalCamera);});
        initAction(actions.getHardFPV(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(CameraMode.HardFPV);});
        initAction(actions.getSoftFPV(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(CameraMode.SoftFPV);});

        initAction(actions.getMap(), () -> simulationState.setMapOverlay(!simulationState.isMapOverlay()));
        initAction(actions.getMapZoomIn(), () -> {if(simulationState.isMapOverlay()) simulationState.setMapZoom(simulationState.getMapZoom() * 1.1f);});
        initAction(actions.getMapZoomOut(), () -> {if(simulationState.isMapOverlay()) simulationState.setMapZoom(simulationState.getMapZoom() * 0.9f);});
    }

    private void initAction(List<BindingConfig.Actions.Binding> actionBindings, Runnable action) throws IOException {
        for (BindingConfig.Actions.Binding binding: actionBindings) {
            if(binding.getAxis() != null)
                axisBindings.add(new AxisBinding(action, binding.getAxis(), binding.getInverse(), binding.getLowerBound(), binding.getUpperBound()));
            if(binding.getButton() != null)
                buttonBindings.add(new ButtonBinding(action, binding.getButton(), binding.getInverse()));
            if(binding.getKey() != null)
                keyboardBindings.add(new KeyboardBinding(action, binding.getKey()));
        }
    }

    public void handleInput() {
        if(simulationState.getCurrentlyControlledDrone() != null) {
            handleJoystick();
            handleKeyboard();
        }
    }

    private void handleKeyboard() {
        keyboardBindings.forEach(binding -> binding.execute(glfwGetKey(simulationState.getWindow(), binding.keyCode)));

    }

    private void handleJoystick() {
        int joystick = 0;
        while(!glfwJoystickPresent(joystick) && joystick < GLFW_JOYSTICK_LAST)
            joystick++;
        if(joystick == GLFW_JOYSTICK_LAST)
            return;
        handleAxes(joystick);
        handleButtons(joystick);
    }


    private void handleButtons(int joystick) {
        ByteBuffer byteBuffer = glfwGetJoystickButtons(joystick);
//        var size = byteBuffer.remaining();
        byte[] arr = new byte[byteBuffer.remaining()];
        byteBuffer.get(arr);

        buttonBindings.forEach(binding -> binding.execute(arr[binding.button]));
//        for(int i=0; i<size; i++)
//            System.out.print(i + ": " + arr[i] +" | ");
//        System.out.println();
    }

    private void handleAxes(int joystick) {
        FloatBuffer floatBuffer = glfwGetJoystickAxes(joystick);
        float[] arr = new float[floatBuffer.remaining()];
        floatBuffer.get(arr);

        axisBindings.forEach(binding -> binding.execute(arr[binding.axis]));

//        debugPrintOutAxes(joystick);
        JoystickStatus joystickStatus = new JoystickStatus();
        steeringAxisBindings.forEach(binding -> joystickStatus.axes.add(convertToRawData(arr[binding.getAxis()], binding)));
        joystickProducer.send(simulationState.getCurrentlyControlledDrone(), joystickStatus);
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

    private void changeControlMode(String controlMode) {
        joystickProducer.send(simulationState.getCurrentlyControlledDrone(), controlMode);
    }

    private float convertToRawData(float axisValue, BindingConfig.SteeringAxis steeringAxis) {
        if(Math.abs(axisValue - steeringAxis.getTrim()) < steeringAxis.getDeadzone())
            return 0;
        float inversedValue = (steeringAxis.getInverse()? -1.0f: 1.0f) * axisValue;
        float normalizedValue = 0.0f;
        if(inversedValue > steeringAxis.getTrim() + steeringAxis.getDeadzone())
            normalizedValue = (inversedValue - (steeringAxis.getDeadzone() + steeringAxis.getTrim())) /
                    (steeringAxis.getMax() - (steeringAxis.getDeadzone() + steeringAxis.getTrim()));
        else if(inversedValue < steeringAxis.getTrim() - steeringAxis.getDeadzone())
            normalizedValue = (inversedValue - (steeringAxis.getTrim() - steeringAxis.getDeadzone())) /
                    ((steeringAxis.getTrim() - steeringAxis.getDeadzone()) - steeringAxis.getMin());
        return Math.max(-1, min(normalizedValue, 1));
    }
}
