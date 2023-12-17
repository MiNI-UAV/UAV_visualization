package org.uav.logic.input.bindingsGeneration;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.uav.logic.config.BindingConfig;
import org.uav.logic.config.Config;
import org.uav.presentation.view.BindingsScreen;
import org.uav.utils.FileMapper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.lwjgl.glfw.GLFW.*;
import static org.uav.logic.input.bindingsGeneration.BindingText.*;

public class BindingsLoop {

    public static final float DEFAULT_AXIS_DEADZONE = 0.1f;
    public static final float DEADZONE_CHANGE_DELTA = 0.01f;
    public static final float AXIS_BOUNDS_CHANGE_DELTA = 0.05f;
    public static final float AXIS_DETECTION_THRESHOLD = 0.2f;
    private final long window;
    private final Config config;
    private final BindingsScreen bindingsScreen;
    private List<String> message;
    private Integer lastKeyPressed;
    private final List<Field> actionsToBind;
    private int actionCurrentlyBinding;
    private BindingsGenerationState state;
    private ActionBindingState actionBindingState;
    private int detectedAxis;
    private Integer detectedAxisForBinding;
    private float detectedAxisForBindingLowerBound;
    private float detectedAxisForBindingUpperBound;
    private float maxDetectedAxis;
    private float minDetectedAxis;
    private float detectedAxisDeadzone;
    private boolean detectedAxisInverse;
    private List<Float> defaultJoystickAxes;
    private List<Byte> defaultJoystickButtons;
    private int joystick;
    private final List<BindingConfig.SteeringAxis> bindedSteeringAxes;
    private final BindingConfig.Actions bindedActions;
    private BindingConfig.Binding detectedActionBinding;
    private final List<List<BindingConfig.Binding>> modesBindings;

    public BindingsLoop(long window, Config config) throws IOException {
        this.window = window;
        this.config = config;
        bindingsScreen = new BindingsScreen(window, config);
        state = BindingsGenerationState.DefaultPositionPrompt;
        actionBindingState = ActionBindingState.BindGeneralActions;
        message = new ArrayList<>();
        lastKeyPressed = null;
        detectedActionBinding = null;
        bindedSteeringAxes = new ArrayList<>();
        modesBindings = new ArrayList<>();
        bindedActions = new BindingConfig.Actions();
        actionCurrentlyBinding = 0;
        detectedAxisDeadzone = DEFAULT_AXIS_DEADZONE;
        actionsToBind = Arrays.asList(BindingConfig.Actions.class.getDeclaredFields());
    }

    public void loop() {
        init();
        while(!glfwWindowShouldClose(window) && state != BindingsGenerationState.Finish) {
            update();
            bindingsScreen.render(message);
        }
        close();
    }

    private void init() {
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(action == GLFW_PRESS)
                lastKeyPressed = key;
        });
    }

    private void close() {
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_PRESS )
                glfwSetWindowShouldClose(window, true);
        });
    }

    private void update() {
        message = new ArrayList<>();
        message.addAll(BINDINGS_GLOBAL_LINES_1_2.apply(config));
        joystick = handleJoystick();
        switch(state) {
            case NoJoystick -> noJoystickPrompt();
            case DefaultPositionPrompt -> defaultPositionPrompt();
            case BindSteeringDetectAxis, BindSteeringSetDeadzone, BindSteeringSetInverse -> bindSteering();
            case BindActionsDetectBinding, BindActionsDetectedAxisSetLowerBound, BindActionsDetectedAxisSetUpperBound -> bindActions();
            case SaveConfig -> saveConfig();
            case Finish -> {}
        }
    }

    private void noJoystickPrompt() {
        message.addAll(BINDINGS_DISCONNECTED_LINES_3_10);
        if(handleKeyPress(GLFW_KEY_ESCAPE))
            state = BindingsGenerationState.Finish;
    }

    private void defaultPositionPrompt() {
        message.addAll(BINDINGS_DEFAULT_POSTION_LINES_3_10);
        if(handleKeyPress(GLFW_KEY_ENTER)) {
            state = BindingsGenerationState.BindSteeringDetectAxis;
            defaultJoystickAxes = getJoystickAxes(joystick);
            defaultJoystickButtons = getJoystickButtons(joystick);
            minDetectedAxis = defaultJoystickAxes.get(0);
            maxDetectedAxis = minDetectedAxis;
        }
        if(handleKeyPress(GLFW_KEY_ESCAPE))
            state = BindingsGenerationState.Finish;
    }

    private void bindSteering() {
        message.addAll(BINDINGS_STEERING_LINES_3_5.apply(bindedSteeringAxes, detectedAxis, minDetectedAxis, maxDetectedAxis, defaultJoystickAxes));
        switch(state) {
            case BindSteeringDetectAxis -> steeringDetectAxis();
            case BindSteeringSetDeadzone -> steeringSetDeadzone();
            case BindSteeringSetInverse -> steeringSetInverse();
        }
    }

    private void steeringSetInverse() {
        message.addAll(BINDINGS_STEERING_INVERSE_LINES_6_10.apply(detectedAxisDeadzone, detectedAxisInverse));
        detectedAxisInverse = sliderInput(detectedAxisInverse, (Boolean b) -> !b, (Boolean b) -> !b);
        if(handleKeyPress(GLFW_KEY_ENTER)) {
            bindedSteeringAxes.add(new BindingConfig.SteeringAxis(
                            detectedAxis,
                            detectedAxisInverse,
                            detectedAxisDeadzone,
                            minDetectedAxis,
                            maxDetectedAxis,
                            defaultJoystickAxes.get(detectedAxis)
            ));
            detectedAxis = 0;
            minDetectedAxis = defaultJoystickAxes.get(detectedAxis);
            maxDetectedAxis = minDetectedAxis;
            state = BindingsGenerationState.BindSteeringDetectAxis;
        }
        if(handleKeyPress(GLFW_KEY_ESCAPE))
            state = BindingsGenerationState.BindSteeringSetDeadzone;
    }

    private void steeringSetDeadzone() {
        message.addAll(BINDINGS_STEERING_DEADZONE_LINES_6_10.apply(detectedAxisDeadzone));
        detectedAxisDeadzone = sliderInput(detectedAxisDeadzone, (Float f) -> f - DEADZONE_CHANGE_DELTA, (Float f) -> f + DEADZONE_CHANGE_DELTA);
        if(handleKeyPress(GLFW_KEY_ENTER)) {
            detectedAxisInverse = false;
            state = BindingsGenerationState.BindSteeringSetInverse;
        }
        if(handleKeyPress(GLFW_KEY_ESCAPE))
            state = BindingsGenerationState.BindSteeringDetectAxis;
    }

    private <T> T sliderInput(T input, Function<T, T> lowerFunction, Function<T,T> riseFunction) {
        if(handleKeyPress(GLFW_KEY_LEFT))
            return lowerFunction.apply(input);
        else if(handleKeyPress(GLFW_KEY_RIGHT))
            return riseFunction.apply(input);
        return input;
    }

    private void steeringDetectAxis() {
        message.addAll(BINDINGS_STEERING_AXIS_LINES_6_10);
        var axes = getJoystickAxes(joystick);
        for(int i=0;i<axes.size(); i++) {
            if(
                Math.abs(defaultJoystickAxes.get(i) - axes.get(i)) -
                Math.abs(defaultJoystickAxes.get(detectedAxis) - axes.get(detectedAxis)) >
                Math.max(Math.abs(maxDetectedAxis - defaultJoystickAxes.get(i)), Math.abs(defaultJoystickAxes.get(i) - minDetectedAxis)) * AXIS_DETECTION_THRESHOLD
            ) {
                detectedAxis = i;
                minDetectedAxis = defaultJoystickAxes.get(i);
                maxDetectedAxis = minDetectedAxis;
                break;
            }
        }
        if(axes.get(detectedAxis) > maxDetectedAxis)
            maxDetectedAxis = axes.get(detectedAxis);
        if(axes.get(detectedAxis) < minDetectedAxis)
            minDetectedAxis = axes.get(detectedAxis);

        if(handleKeyPress(GLFW_KEY_ENTER)) {
            state = BindingsGenerationState.BindSteeringSetDeadzone;
        }
        if(handleKeyPress(GLFW_KEY_ESCAPE))
            state = BindingsGenerationState.BindActionsDetectBinding;
    }

    private void bindActions() {
        List<BindingConfig.Binding> currentlyBindingActionList = null;
        switch(actionBindingState) {
            case BindGeneralActions -> {
                try {
                    actionsToBind.get(actionCurrentlyBinding).setAccessible(true);
                    currentlyBindingActionList = ((List<BindingConfig.Binding>)actionsToBind.get(actionCurrentlyBinding).get(bindedActions));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                message.addAll(BINDINGS_GENERAL_ACTIONS_LINE_3.apply(currentlyBindingActionList, actionsToBind, actionCurrentlyBinding));
            }
            case BindModes -> message.addAll(BINDINGS_ACTIONS_MODES_LINE_3.apply(modesBindings));
        }
        switch(state) {
            case BindActionsDetectBinding -> {
                if(detectedAxisForBinding != null) message.addAll(BINDINGS_ACTIONS_DETECTED_AXIS_LINES_4_9.apply(detectedAxisForBinding));
                else if(detectedActionBinding != null && detectedActionBinding.getButton() != null)
                    message.addAll(BINDINGS_ACTIONS_DETECTED_BUTTON_LINES_4_9.apply(detectedActionBinding));
                else if(detectedActionBinding != null && detectedActionBinding.getKey() != null)
                    message.addAll(BINDINGS_ACTIONS_DETECTED_KEY_LINES_4_9.apply(detectedActionBinding));
                else message.addAll(BINDINGS_ACTIONS_DETECTED_NOTHING_LINES_4_9);
                if(actionBindingState == ActionBindingState.BindModes && modesBindings.get(modesBindings.size() - 1).isEmpty())
                    message.addAll(BINDINGS_ACTIONS_FINISH_LINE_10);
                else message.addAll(BINDINGS_ACTIONS_NEXT_LINE_10);

            }
            case BindActionsDetectedAxisSetLowerBound -> {
                var axes = getJoystickAxes(joystick);
                message.addAll(BINDINGS_ACTIONS_AXIS_LOWER_BOUND_LINES_4_10.apply(axes, detectedAxisForBinding, detectedAxisForBindingLowerBound));
            }
            case BindActionsDetectedAxisSetUpperBound -> {
                var axes = getJoystickAxes(joystick);
                message.addAll(BINDINGS_ACTIONS_AXIS_UPPER_BOUND_LINES_4_10.apply(axes, detectedAxisForBinding, detectedAxisForBindingLowerBound, detectedAxisForBindingUpperBound));
            }
        }


        switch(state) {
            case BindActionsDetectBinding -> bindActionsGeneral(currentlyBindingActionList);
            case BindActionsDetectedAxisSetLowerBound -> bindActionsDetectedAxisLowerBound();
            case BindActionsDetectedAxisSetUpperBound -> bindActionsDetectedAxisUpperBound(currentlyBindingActionList);
        }
    }

    private void bindActionsDetectedAxisUpperBound(List<BindingConfig.Binding> currentlyBindingActionList) {
        detectedAxisForBindingUpperBound = sliderInput(detectedAxisForBindingUpperBound, (Float f) -> f - AXIS_BOUNDS_CHANGE_DELTA, (Float f) -> f + AXIS_BOUNDS_CHANGE_DELTA);
        if(handleKeyPress(GLFW_KEY_ENTER)) {
            var binding = new BindingConfig.Binding(detectedAxisForBinding, detectedAxisForBindingLowerBound , detectedAxisForBindingUpperBound, null, null, null);
            switch (actionBindingState) {
                case BindGeneralActions -> currentlyBindingActionList.add(binding);
                case BindModes -> modesBindings.get(modesBindings.size() - 1).add(binding);
            }
            detectedAxisForBinding = null;
            detectedActionBinding = null;
            state = BindingsGenerationState.BindActionsDetectBinding;
        }
        if(handleKeyPress(GLFW_KEY_ESCAPE)) {
            state = BindingsGenerationState.BindActionsDetectedAxisSetLowerBound;
        }
    }

    private void bindActionsDetectedAxisLowerBound() {
        detectedAxisForBindingLowerBound = sliderInput(detectedAxisForBindingLowerBound, (Float f) -> f - AXIS_BOUNDS_CHANGE_DELTA, (Float f) -> f + AXIS_BOUNDS_CHANGE_DELTA);
        if(handleKeyPress(GLFW_KEY_ENTER)) {
            detectedAxisForBindingUpperBound = 0;
            state = BindingsGenerationState.BindActionsDetectedAxisSetUpperBound;
        }
        if(handleKeyPress(GLFW_KEY_ESCAPE)) {
            detectedAxisForBinding = null;
            state = BindingsGenerationState.BindActionsDetectBinding;
        }
    }

    private void bindActionsGeneral(List<BindingConfig.Binding> currentlyBindingActionList) {
        // Check keyboard
        if(lastKeyPressed != null && lastKeyPressed != GLFW_KEY_ENTER && lastKeyPressed != GLFW_KEY_ESCAPE) {
            detectedAxisForBinding = null;
            detectedActionBinding = new BindingConfig.Binding(null, null, null, null, null, lastKeyPressed);
            lastKeyPressed = null;
        }
        // Check buttons
        var buttons = getJoystickButtons(joystick);
        for(int i=0;i<buttons.size(); i++) {
            if(!Objects.equals(defaultJoystickButtons.get(i), buttons.get(i))) {
                detectedAxisForBinding = null;
                detectedActionBinding = new BindingConfig.Binding(null, null , null, i, defaultJoystickButtons.get(i) == 1, null);
            }
        }
        // Check axis
        var axes = getJoystickAxes(joystick);
        for(int i=0;i<axes.size(); i++) {
            if(Math.abs(defaultJoystickAxes.get(i) - axes.get(i)) > 0.2)
                detectedAxisForBinding = i;
        }
        if(detectedAxisForBinding != null && handleKeyPress(GLFW_KEY_ENTER)) {
            state = BindingsGenerationState.BindActionsDetectedAxisSetLowerBound;
            detectedAxisForBindingLowerBound = 0;
        }
        if(detectedActionBinding != null && handleKeyPress(GLFW_KEY_ENTER)) {
            switch (actionBindingState) {
                case BindGeneralActions -> currentlyBindingActionList.add(detectedActionBinding);
                case BindModes -> modesBindings.get(modesBindings.size() - 1).add(detectedActionBinding);
            }
            detectedActionBinding = null;
        }
        if(handleKeyPress(GLFW_KEY_ESCAPE)) {
            switch(actionBindingState) {
                case BindGeneralActions -> {
                    actionCurrentlyBinding++;
                    if(actionCurrentlyBinding >= actionsToBind.size()) {
                        actionBindingState = ActionBindingState.BindModes;
                        modesBindings.add(new ArrayList<>());
                    }
                    detectedAxisForBinding = null;
                }
                case BindModes -> {
                    if(modesBindings.get(modesBindings.size() - 1).isEmpty()) {
                        state = BindingsGenerationState.SaveConfig;
                        modesBindings.remove(modesBindings.size()-1);
                    }
                    else modesBindings.add(new ArrayList<>());
                    detectedAxisForBinding = null;
                }
            }
            detectedActionBinding = null;
        }
    }

    private void saveConfig() {
        var b = new BindingConfig(bindedSteeringAxes, bindedActions, modesBindings);
        FileMapper.save(b, Paths.get(System.getProperty("user.dir"), config.getBindingsConfig().getSource()), new YAMLMapper());
        state = BindingsGenerationState.Finish;
    }

    private List<Byte> getJoystickButtons(int joystick) {
        ByteBuffer byteBuffer = glfwGetJoystickButtons(joystick);
        var buttons = new ArrayList<Byte>();
        while(byteBuffer.hasRemaining())
            buttons.add(byteBuffer.get());
        return buttons;
    }

    private List<Float> getJoystickAxes(int joystick) {
        FloatBuffer floatBuffer = glfwGetJoystickAxes(joystick);
        var axes = new ArrayList<Float>();
        while(floatBuffer.hasRemaining())
            axes.add(floatBuffer.get());
        return axes;

    }

    private int handleJoystick() {
        int joystick = 0;
        while(!glfwJoystickPresent(joystick) && joystick < GLFW_JOYSTICK_LAST)
            joystick++;
        if(joystick == GLFW_JOYSTICK_LAST) state = BindingsGenerationState.NoJoystick;
        else if(state == BindingsGenerationState.NoJoystick) state = BindingsGenerationState.DefaultPositionPrompt;
        return joystick;
    }

    private boolean handleKeyPress(int key) {
        if(lastKeyPressed != null && lastKeyPressed == key){
            lastKeyPressed = null;
            return true;
        }
        return false;
    }

    public enum ActionBindingState {
        BindGeneralActions,
        BindModes
    }

    public enum BindingsGenerationState {
        NoJoystick,
        DefaultPositionPrompt,
        BindSteeringDetectAxis,
        BindSteeringSetDeadzone,
        BindSteeringSetInverse,
        BindActionsDetectBinding,
        BindActionsDetectedAxisSetLowerBound,
        BindActionsDetectedAxisSetUpperBound,
        SaveConfig,
        Finish
    }

}
