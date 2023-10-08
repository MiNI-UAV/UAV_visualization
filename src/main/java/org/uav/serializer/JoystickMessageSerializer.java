package org.uav.serializer;

import org.uav.model.status.JoystickStatus;
import org.uav.queue.Actions;

import java.text.MessageFormat;
import java.util.Locale;

public class JoystickMessageSerializer implements MessageSerializer {

    @Override
    public String serialize(JoystickStatus status) {
        Locale.setDefault(Locale.US);
        return "j:" + String.join(",", status.axes.stream().map(axis -> Float.toString(axis)).toList());
    }

    @Override
    public String serialize(String mode) {
        Locale.setDefault(Locale.US);
        String msg = MessageFormat.format("m:{0}", mode);
        //System.out.println(msg);
        return msg;
    }

    @Override
    public String serialize(Actions actions) {
        Locale.setDefault(Locale.US);
        String msg = MessageFormat.format("{0}", actions.toMessage());
        //System.out.println(msg);
        return msg;
    }
}
