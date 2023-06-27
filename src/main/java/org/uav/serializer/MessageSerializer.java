package org.uav.serializer;

import org.uav.queue.ControlModes;
import org.uav.status.JoystickStatus;

public interface MessageSerializer {
    String serialize(JoystickStatus status);
    String serialize(ControlModes mode);
}
