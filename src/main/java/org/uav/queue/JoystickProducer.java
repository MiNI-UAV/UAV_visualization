package org.uav.queue;

import org.uav.model.Drone;
import org.uav.model.status.JoystickStatus;
import org.uav.serializer.JoystickMessageSerializer;

public class JoystickProducer {
    private final JoystickMessageSerializer messageSerializer;

    public JoystickProducer() {
        messageSerializer = new JoystickMessageSerializer();
    }

    public void send(Drone drone, JoystickStatus joystickStatus) {
        String message = messageSerializer.serialize(joystickStatus);
        drone.sendSteeringCommand(message);
        //System.out.println("Sent: [" + message + "]");
    }

    public void send(Drone drone, ControlMode mode) {
        String message = messageSerializer.serialize(mode);
        drone.sendSteeringCommand(message);
//        System.out.println("Sent: [" + message + "]");
    }

    public void send(Drone drone, Actions actions) {
        String message = messageSerializer.serialize(actions);
        drone.sendUtilsCommand(message);
        // System.out.println("Sent: [" + message + "]");
    }
}
