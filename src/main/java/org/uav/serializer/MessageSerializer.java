package org.uav.serializer;

import org.uav.queue.Actions;
import org.uav.queue.ControlMode;
import org.uav.model.status.JoystickStatus;

public interface MessageSerializer {
    String serialize(JoystickStatus status);
    String serialize(ControlMode mode);

    String serialize(Actions action);
}
