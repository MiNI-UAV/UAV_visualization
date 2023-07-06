package org.uav.serializer;

import org.uav.queue.Actions;
import org.uav.queue.ControlModes;
import org.uav.model.status.JoystickStatus;

public interface MessageSerializer {
    String serialize(JoystickStatus status);
    String serialize(ControlModes mode);

    String serialize(Actions action);
}
