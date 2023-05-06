package org.uav.serializer;

import org.uav.status.JoystickStatus;

import java.text.MessageFormat;
import java.util.Locale;

public class JoystickMessageSerializer implements MessageSerializer<JoystickStatus> {

    @Override
    public String serialize(JoystickStatus status) {
        Locale.setDefault(Locale.US);
        return MessageFormat.format(
                "j:{0,number,#.###},{1,number,#.###},{2,number,#.###},{3,number,#.###}",
                status.rawData[0], status.rawData[1], status.rawData[2], status.rawData[3]);
    }
}
