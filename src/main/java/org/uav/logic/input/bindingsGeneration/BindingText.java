package org.uav.logic.input.bindingsGeneration;

import org.uav.logic.config.BindingConfig;
import org.uav.logic.config.Config;
import org.uav.utils.PentaFunction;
import org.uav.utils.TetraFunction;
import org.uav.utils.TriFunction;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BindingText {
    public static final String EMPTY_LINE = "";
    public static final String CONTINUE = "Continue (Enter)";
    public static final String NEXT = "Next (Enter)";
    public static final String BACK = "Back (Esc)";

    public static final Function<Config, List<String>> BINDINGS_GLOBAL_LINES_1_2 = (Config c) ->
            List.of("Setting up bindings config for " + c.getBindingsConfig().getSource(),EMPTY_LINE);

    public static final List<String> BINDINGS_DISCONNECTED_LINES_3_10 = List.of(
            "Joystick disconnected. Please connect the joystick first",
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            "Skip config generation (ESC)"
    );
    public static final List<String> BINDINGS_DEFAULT_POSTION_LINES_3_10 = List.of(
            "Put joystick in the default position",
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            CONTINUE,
            "Skip config generation (ESC)"
            );

    private static final Map<Integer, String> AXIS_NAME_MAPPING = new HashMap<>()
    {{put(0, "Throttle"); put(1, "Roll"); put(2, "Pitch"); put(3, "Yaw");}};
    public static final PentaFunction<List<BindingConfig.SteeringAxis>, Integer, Float, Float, List<Float>, List<String>> BINDINGS_STEERING_LINES_3_5 = (
            List<BindingConfig.SteeringAxis> bindedSteeringAxes,
            Integer detectedAxis,
            Float minDetectedAxis,
            Float maxDetectedAxis,
            List<Float> defaultJoystickAxes
    ) -> {
        String axisName = AXIS_NAME_MAPPING.getOrDefault(bindedSteeringAxes.size(), "Axis" + (bindedSteeringAxes.size() + 1));
        return List.of(
                "Assign axis no. " + (bindedSteeringAxes.size() + 1) + " (" + axisName + ")",
                "Detected axis: " + detectedAxis,
                MessageFormat.format("Min: {0,number,#.###} | Max: {1,number,#.###} | Trim: {2,number,#.###}", minDetectedAxis, maxDetectedAxis, defaultJoystickAxes.get(detectedAxis))
        );
    };

    public static final List<String> BINDINGS_STEERING_AXIS_LINES_6_10 = List.of(
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            NEXT,
            "End steering binding and continue to actions (ESC)"
    );

    public static final Function<Float, List<String>> BINDINGS_STEERING_DEADZONE_LINES_6_10 =
            (Float detectedAxisDeadzone) -> List.of(
                    MessageFormat.format("Deadzone:  < {0,number,#.##} >", detectedAxisDeadzone),
                    EMPTY_LINE,
                    EMPTY_LINE,
                    NEXT,
                    BACK
            );

    public static final BiFunction<Float, Boolean, List<String>> BINDINGS_STEERING_INVERSE_LINES_6_10 =
            (Float detectedAxisDeadzone, Boolean detectedAxisInverse) -> List.of(
                    MessageFormat.format("Deadzone:    {0,number,#.##}", detectedAxisDeadzone),
                    MessageFormat.format("Inverse:  < {0} >", detectedAxisInverse),
                    EMPTY_LINE,
                    "Confirm axis and continue to the next (Enter)",
                    BACK
            );

    public static final TriFunction<List<BindingConfig.Binding>, List<Field>, Integer, List<String>> BINDINGS_GENERAL_ACTIONS_LINE_3 =
            (List<BindingConfig.Binding> currentlyBindingActionList, List<Field> actionsToBind, Integer actionCurrentlyBinding) -> List.of(
                    "Assign binding no. " + (currentlyBindingActionList.size() + 1) + " for " + actionsToBind.get(actionCurrentlyBinding).getName()
            );

    public static final Function<List<List<BindingConfig.Binding>>, List<String>> BINDINGS_ACTIONS_MODES_LINE_3 =
            (List<List<BindingConfig.Binding>> modesBindings) -> List.of(
                    "Assign binding no. " + (modesBindings.get(modesBindings.size() - 1).size() + 1) + " for mode no. " + (modesBindings.size())
            );

    public static final Function<Integer, List<String>> BINDINGS_ACTIONS_DETECTED_AXIS_LINES_4_9 = (Integer detectedAxisForBinding) -> List.of(
            "Axis: " + detectedAxisForBinding,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            "Set up bounds for detected axis (Enter)"
    );

    public static final Function<BindingConfig.Binding, List<String>> BINDINGS_ACTIONS_DETECTED_BUTTON_LINES_4_9 =
            (BindingConfig.Binding detectedActionBinding) -> List.of(
                    MessageFormat.format("Button: {0} | Inverse: {1}", detectedActionBinding.getButton(), detectedActionBinding.getInverse()),
                    EMPTY_LINE,
                    EMPTY_LINE,
                    EMPTY_LINE,
                    EMPTY_LINE,
                    "Assign the button to the action (Enter)"
            );

    public static final Function<BindingConfig.Binding, List<String>> BINDINGS_ACTIONS_DETECTED_KEY_LINES_4_9 =
            (BindingConfig.Binding detectedActionBinding) -> List.of(
                    MessageFormat.format("Key: {0}", detectedActionBinding.getKey()),
                    EMPTY_LINE,
                    EMPTY_LINE,
                    EMPTY_LINE,
                    EMPTY_LINE,
                    "Assign the key to the action (Enter)"
            );

    public static final List<String> BINDINGS_ACTIONS_DETECTED_NOTHING_LINES_4_9 = List.of(
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE,
            EMPTY_LINE
    );

    public static final List<String> BINDINGS_ACTIONS_NEXT_LINE_10 = List.of(
            "Continue to the next action (ESC)"
    );

    public static final List<String> BINDINGS_ACTIONS_FINISH_LINE_10 = List.of(
            "Finish binding generation and save result (ESC)"
    );

    public static final TriFunction<List<Float>, Integer, Float, List<String>> BINDINGS_ACTIONS_AXIS_LOWER_BOUND_LINES_4_10 =
            (List<Float> axes, Integer detectedAxisForBinding, Float detectedAxisForBindingLowerBound) -> List.of(
                    "Axis: " + detectedAxisForBinding + " (" + axes.get(detectedAxisForBinding) + ")",
                    MessageFormat.format("Lower bound:  < {0,number,#.##} >", detectedAxisForBindingLowerBound),
                    EMPTY_LINE,
                    EMPTY_LINE,
                    EMPTY_LINE,
                    NEXT,
                    BACK
            );

    public static final TetraFunction<List<Float>, Integer, Float, Float, List<String>> BINDINGS_ACTIONS_AXIS_UPPER_BOUND_LINES_4_10 =
            (List<Float> axes, Integer detectedAxisForBinding, Float detectedAxisForBindingLowerBound, Float detectedAxisForBindingUpperBound) -> List.of(
                    "Axis: " + detectedAxisForBinding + " (" + axes.get(detectedAxisForBinding) + ")",
                    MessageFormat.format("Lower bound:  < {0,number,#.##} >", detectedAxisForBindingLowerBound),
                    MessageFormat.format("Upper bound:  < {0,number,#.##} >", detectedAxisForBindingUpperBound),
                    EMPTY_LINE,
                    EMPTY_LINE,
                    "Assign the binding to the action (Enter)",
                    BACK
            );

}
