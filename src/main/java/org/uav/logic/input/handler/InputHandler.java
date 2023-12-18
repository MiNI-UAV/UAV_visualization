package org.uav.logic.input.handler;

import org.uav.logic.audio.MusicPlayer;
import org.uav.logic.communication.JoystickProducer;
import org.uav.logic.config.BindingConfig;
import org.uav.logic.config.Config;
import org.uav.logic.input.binding.AxisBinding;
import org.uav.logic.input.binding.ButtonBinding;
import org.uav.logic.input.binding.KeyboardBinding;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.logic.state.simulation.SimulationStateProcessor;
import org.uav.presentation.entity.camera.CameraMode;

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
    private final MusicPlayer musicPlayer;


    public InputHandler(
            SimulationStateProcessor simulationStateProcessor,
            SimulationState simulationState,
            Config config,
            BindingConfig bindingConfig,
            MusicPlayer musicPlayer
    ) throws IOException {
        this.config = config;
        this.simulationState = simulationState;
        this.simulationStateProcessor = simulationStateProcessor;
        this.musicPlayer = musicPlayer;
        joystickProducer = new JoystickProducer();
        axisBindings = new ArrayList<>();
        buttonBindings = new ArrayList<>();
        keyboardBindings = new ArrayList<>();
        steeringAxisBindings = new ArrayList<>();
        initBindings(bindingConfig);
    }

    private void initBindings(BindingConfig bindingConfig) throws IOException {
        // Steering
        steeringAxisBindings.addAll(bindingConfig.getSteering());
        // Actions
        for(int i=0; i<bindingConfig.getModes().size(); i++) {
            var modeId = i;
            var mode = bindingConfig.getModes().get(modeId);
            initAction(mode, () -> {if(!simulationState.isMapOverlay() && modeId < config.getDroneSettings().getModes().size()) changeControlMode(config.getDroneSettings().getModes().get(modeId));});
        }
        var actions = bindingConfig.getActions();
        initAction(actions.getShoot(), () -> {if(!simulationState.isMapOverlay() && simulationState.getCurrentlyControlledDrone().isPresent()) simulationState.getAmmos().get(simulationState.getCurrentlyChosenAmmo()).parseProjectileMessage(joystickProducer.send(simulationState.getCurrentlyControlledDrone().get(), Action.shoot, simulationState.getCurrentlyChosenAmmo()));});
        initAction(actions.getDrop(), () ->{if(!simulationState.isMapOverlay() && simulationState.getCurrentlyControlledDrone().isPresent()) simulationState.getCargos().get(simulationState.getCurrentlyChosenCargo()).parseProjectileMessage(joystickProducer.send(simulationState.getCurrentlyControlledDrone().get(), Action.drop, simulationState.getCurrentlyChosenCargo()));});
        initAction(actions.getRelease(), () ->{if(!simulationState.isMapOverlay() && simulationState.getCurrentlyControlledDrone().isPresent()) joystickProducer.send(simulationState.getCurrentlyControlledDrone().get(), Action.release);});
        initAction(actions.getPrevCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(simulationState.getCurrentCameraMode().prev());});
        initAction(actions.getNextCamera(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentCameraMode(simulationState.getCurrentCameraMode().next());});
        initAction(actions.getRespawn(), () -> {if(!simulationState.isMapOverlay()) simulationStateProcessor.respawnDrone();});
        initAction(actions.getNextAmmo(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentlyChosenAmmo((simulationState.getCurrentlyChosenAmmo() == simulationState.getAmmos().size()-1)? 0: simulationState.getCurrentlyChosenAmmo()+1);});
        initAction(actions.getNextCargo(), () -> {if(!simulationState.isMapOverlay()) simulationState.setCurrentlyChosenCargo((simulationState.getCurrentlyChosenCargo() == simulationState.getCargos().size()-1)? 0: simulationState.getCurrentlyChosenCargo()+1);});
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

        initAction(actions.getToggleRadio(), musicPlayer::playOrStop);
        initAction(actions.getNextSong(), musicPlayer::nextSong);

        initAction(actions.getToggleSpotLight(), () -> simulationState.setSpotLightOn(!simulationState.isSpotLightOn()));
    }

    private void initAction(List<BindingConfig.Binding> actionBindings, Runnable action) throws IOException {
        for (BindingConfig.Binding binding: actionBindings) {
            if(binding.getAxis() != null)
                axisBindings.add(new AxisBinding(action, binding.getAxis(), binding.getLowerBound(), binding.getUpperBound()));
            if(binding.getButton() != null)
                buttonBindings.add(new ButtonBinding(action, binding.getButton(), binding.getInverse()));
            if(binding.getKey() != null)
                keyboardBindings.add(new KeyboardBinding(action, binding.getKey()));
        }
    }

    public void handleInput() {
        handleJoystick();
        handleKeyboard();
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
        if(byteBuffer == null) return;
        byte[] arr = new byte[byteBuffer.remaining()];
        byteBuffer.get(arr);

        buttonBindings.forEach(binding -> binding.execute(arr[binding.button]));
    }

    private void handleAxes(int joystick) {
        FloatBuffer floatBuffer = glfwGetJoystickAxes(joystick);
        if(floatBuffer == null) return;
        float[] arr = new float[floatBuffer.remaining()];
        floatBuffer.get(arr);

        axisBindings.forEach(binding -> binding.execute(arr[binding.getAxis()]));

        JoystickStatus joystickStatus = new JoystickStatus();
        steeringAxisBindings.forEach(binding -> joystickStatus.axes.add(convertToRawData(arr[binding.getAxis()], binding)));
        simulationState.setJoystickStatus(joystickStatus);
        if(simulationState.getCurrentlyControlledDrone().isPresent())
            joystickProducer.send(simulationState.getCurrentlyControlledDrone().get(), joystickStatus);
    }

    private void changeControlMode(String controlMode) {
        if(simulationState.getCurrentlyControlledDrone().isPresent())
            joystickProducer.send(simulationState.getCurrentlyControlledDrone().get(), controlMode);
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
