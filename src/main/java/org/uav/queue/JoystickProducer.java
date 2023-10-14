package org.uav.queue;

import org.uav.model.Drone;
import org.uav.model.SimulationState;
import org.uav.model.status.JoystickStatus;
import org.uav.serializer.JoystickMessageSerializer;

public class JoystickProducer {
    private final JoystickMessageSerializer messageSerializer;
    private final SimulationState simulationState;

    public JoystickProducer(SimulationState simulationState) {
        messageSerializer = new JoystickMessageSerializer();
        this.simulationState = simulationState;
    }

    public void send(Drone drone, JoystickStatus joystickStatus) {
        String message = messageSerializer.serialize(joystickStatus);
        drone.sendSteeringCommand(message);
        //System.out.println("Sent: [" + message + "]");
    }

    public void send(Drone drone, String mode) {
        String message = messageSerializer.serialize(mode);
        drone.sendSteeringCommand(message);
//        System.out.println("Sent: [" + message + "]");
    }

    public void send(Drone drone, Action action, Integer ...params) {
        String message = messageSerializer.serializeWithParams(action, params);
        System.out.println("Sent: [" + message + "]");
        String reply = drone.sendUtilsCommand(message);
        System.out.println("Received: [" + reply + "]");
    }
}
