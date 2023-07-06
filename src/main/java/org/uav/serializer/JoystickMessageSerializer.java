package org.uav.serializer;

import org.uav.queue.Actions;
import org.uav.queue.ControlModes;
import org.uav.model.status.JoystickStatus;

import java.text.MessageFormat;
import java.util.Locale;

public class JoystickMessageSerializer implements MessageSerializer {

    @Override
    public String serialize(JoystickStatus status) {
        Locale.setDefault(Locale.US);
        return MessageFormat.format(
                "j:{0,number,#.###},{1,number,#.###},{2,number,#.###},{3,number,#.###}",
                status.rawData[0], status.rawData[1], status.rawData[2], status.rawData[3]);
    }

    @Override
    public String serialize(ControlModes mode) {
        Locale.setDefault(Locale.US);
        String msg = MessageFormat.format("m:{0}", mode.toString());
        //System.out.println(msg);
        return msg;
    }

    @Override
    public String serialize(Actions actions) {
        Locale.setDefault(Locale.US);
        String msg = MessageFormat.format("{0}", actions.toString());
        //System.out.println(msg);
        return msg;
    }
}
