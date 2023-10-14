package org.uav.serializer;

import org.uav.model.status.JoystickStatus;
import org.uav.queue.Action;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class JoystickMessageSerializer {

    public String serialize(JoystickStatus status) {
        Locale.setDefault(Locale.US);
        return "j:" + String.join(",", status.axes.stream().map(axis -> Float.toString(axis)).toList());
    }

    public String serialize(String mode) {
        Locale.setDefault(Locale.US);
        String msg = MessageFormat.format("m:{0}", mode);
        //System.out.println(msg);
        return msg;
    }

    public String serializeWithParams(Action action, Integer ...params) {
        Locale.setDefault(Locale.US);
        String msg = action.toMessage() + ';' + Arrays.stream(params).map(i -> Integer.toString(i)).collect(Collectors.joining(","));
        //System.out.println(msg);
        return msg;
    }
}
