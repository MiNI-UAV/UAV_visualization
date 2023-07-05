package org.uav.queue;

public class DroneRequestReplyMessage {

    int droneId;
    int steerPort;

    int utilsPort;

    public DroneRequestReplyMessage(int droneId, int steerPort, int utilsPort) {
        this.droneId = droneId;
        this.steerPort = steerPort;
        this.utilsPort = utilsPort;
    }
}
