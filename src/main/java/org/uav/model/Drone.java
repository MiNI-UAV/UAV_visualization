package org.uav.model;

import org.uav.config.Configuration;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Drone {

    public int id;
    public ZMQ.Socket socket;

    public Drone(ZContext context, int dronePort, int droneId, Configuration configuration) {
        id = droneId;
        socket = context.createSocket(SocketType.PAIR);
        String address = "tcp://" + configuration.address + ":" + dronePort;
        System.out.println(address);
        socket.connect(address);
    }

    public void sendSteeringCommand(String command) {
        socket.send(command.getBytes(ZMQ.CHARSET), 0);
    }
}
