package org.uav.logic.communication;

import org.uav.logic.input.handler.Action;
import org.uav.logic.input.handler.JoystickStatus;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class JoystickProducer {

    public void send(DroneCommunication drone, JoystickStatus joystickStatus) {
        String message = serialize(joystickStatus);
        drone.sendSteeringCommand(message);
    }

    public void send(DroneCommunication drone, String mode) {
        String message = serialize(mode);
        drone.sendSteeringCommand(message);
    }

    public String send(DroneCommunication drone, Action action, Integer ...params) {
        String message = serializeWithParams(action, params);
        return drone.sendUtilsCommand(message);
    }

    private String serialize(JoystickStatus status) {
        Locale.setDefault(Locale.US);
        return "j:" + String.join(",", status.axes.stream().map(axis -> Float.toString(axis)).toList());
    }

    private String serialize(String mode) {
        Locale.setDefault(Locale.US);
        return MessageFormat.format("m:{0}", mode);
    }

    private String serializeWithParams(Action action, Integer ...params) {
        Locale.setDefault(Locale.US);
        return action.toMessage() + ';' + Arrays.stream(params).map(i -> Integer.toString(i)).collect(Collectors.joining(","));
    }
}
