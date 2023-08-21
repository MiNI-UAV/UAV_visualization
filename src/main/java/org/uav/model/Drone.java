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

        steerSocket = context.createSocket(SocketType.REQ);
        String address = "tcp://" + config.serverAddress + ":" + steerPort;
        steerSocket.connect(address);

        utilsSocket = context.createSocket(SocketType.PAIR);
        String address2 = "tcp://" + config.serverAddress + ":" + utilsPort;
        utilsSocket.connect(address2);
    }

    public void sendSteeringCommand(String command) {
        steerSocket.send(command.getBytes(ZMQ.CHARSET), 0);
        byte[] reply = steerSocket.recv(0);
        String message = new String(reply, ZMQ.CHARSET);
        System.out.println("Received: [" + message + "]");
    }
    public void sendUtilsCommand(String command) {
        utilsSocket.send(command.getBytes(ZMQ.CHARSET), 0);
    }
}
