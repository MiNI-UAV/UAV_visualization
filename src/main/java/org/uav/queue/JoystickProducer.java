package org.uav.queue;

import org.uav.model.Drone;
import org.uav.status.JoystickStatus;
import org.uav.serializer.JoystickMessageSerializer;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JoystickProducer {
    private final JoystickMessageSerializer messageSerializer;

    public JoystickProducer(ZContext context) {
        messageSerializer = new JoystickMessageSerializer();
    }

    public void send(Drone drone, JoystickStatus joystickStatus) {
        String message = messageSerializer.serialize(joystickStatus);
        drone.sendSteeringCommand(message);
        System.out.println("Sent: [" + message + "]");
    }

    public void send(Drone drone, ControlModes mode) {
        String message = messageSerializer.serialize(mode);
        drone.sendSteeringCommand(message);
        System.out.println("Sent: [" + message + "]");
    }
}
