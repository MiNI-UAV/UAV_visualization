package org.uav.serializer;

import org.uav.model.status.JoystickStatus;
import org.uav.queue.Actions;

public interface MessageSerializer {
    String serialize(JoystickStatus status);
    String serialize(String mode);

    String serialize(Actions action);
}
