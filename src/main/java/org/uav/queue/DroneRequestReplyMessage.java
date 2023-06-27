package org.uav.queue;

public class DroneRequestReplyMessage {

    int droneId;
    int dronePort;

    public DroneRequestReplyMessage(int droneId, int dronePort) {
        this.droneId = droneId;
        this.dronePort = dronePort;
    }
}
