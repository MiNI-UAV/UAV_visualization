package org.uav.model;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Drone {

    private static final String address = "tcp://127.0.0.1:";
    public int id;
    public ZMQ.Socket socket;

    public Drone(ZContext context, int dronePort, int droneId) {
        id = droneId;
        socket = context.createSocket(SocketType.PAIR);
        socket.connect(address + dronePort);
    }

    public void sendSteeringCommand(String command) {
        socket.send(command.getBytes(ZMQ.CHARSET), 0);
    }
}
