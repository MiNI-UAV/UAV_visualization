package org.uav.serializer;

import org.uav.status.JoystickStatus;

import java.text.MessageFormat;
import java.util.Locale;

public class JoystickMessageSerializer implements MessageSerializer<JoystickStatus> {

    @Override
    public String serialize(JoystickStatus status) {
        Locale.setDefault(Locale.US);
        return MessageFormat.format(
                "Z:{0,number,#.###},F:{1,number,#.###},T:{2,number,#.###},P:{3,number,#.###}",
                status.z, status.roll, status.pitch, status.yaw);
    }
}
