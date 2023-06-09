package org.uav.model;

import org.uav.config.Config;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Drone {

    public int id;
    public ZMQ.Socket steerSocket;
    public ZMQ.Socket utilsSocket;

    public Drone(ZContext context, int steerPort, int utilsPort, int droneId, Config config) {
        id = droneId;

        steerSocket = context.createSocket(SocketType.PAIR);
        String address = "tcp://" + config.serverAddress + ":" + steerPort;
        System.out.println(address);
        steerSocket.connect(address);

        utilsSocket = context.createSocket(SocketType.PAIR);
        String address2 = "tcp://" + config.serverAddress + ":" + utilsPort;
        System.out.println(address2);
        utilsSocket.connect(address2);
    }

    public void sendSteeringCommand(String command) {
        steerSocket.send(command.getBytes(ZMQ.CHARSET), 0);
    }
    public void sendUtilsCommand(String command) {
        utilsSocket.send(command.getBytes(ZMQ.CHARSET), 0);
    }
}
