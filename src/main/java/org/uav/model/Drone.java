package org.uav.model;

import lombok.Getter;
import org.joml.Vector4f;
import org.uav.config.Config;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

public class Drone {

    @Getter
    private final int id;
    private final ZMQ.Socket steerSocket;
    private final ZMQ.Socket utilsSocket;
    private final SimulationState simulationState;

    public Drone(ZContext context, int steerPort, int utilsPort, int droneId, SimulationState simulationState, Config config) {
        this.simulationState = simulationState;
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
        parseSteeringCommand(message);
    }

    private void parseSteeringCommand(String message) {
        int commaIdx = message.indexOf(',');
        commaIdx = commaIdx == -1? message.length(): commaIdx;
        switch(message.substring(0, commaIdx)) {
            case "ok" -> {}
            case "Position" -> simulationState.setPositionalModeDemands(parseControlModeMessage(message.substring(commaIdx)));
            case "Angle" -> simulationState.setAngleModeDemands(parseControlModeMessage(message.substring(commaIdx)));
            case "Acro" -> simulationState.setAcroModeDemands(parseControlModeMessage(message.substring(commaIdx)));
        }
    }

    private Vector4f parseControlModeMessage(String message) {
        Scanner scanner = new Scanner(message);
        scanner.useDelimiter(",");
        float f1 = Float.parseFloat(scanner.next());
        float f2 = Float.parseFloat(scanner.next());
        float f3 = Float.parseFloat(scanner.next());
        float f4 = Float.parseFloat(scanner.next());
        return new Vector4f(f1, f2, f3, f4);
    }

    public void sendUtilsCommand(String command) {
        utilsSocket.send(command.getBytes(ZMQ.CHARSET), 0);
    }
}
