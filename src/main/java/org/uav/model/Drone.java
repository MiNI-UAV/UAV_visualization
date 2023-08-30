package org.uav.model;

import lombok.Getter;
import org.joml.Vector4f;
import org.uav.config.Config;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Scanner;

import static org.uav.utils.ZmqUtils.checkErrno;

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
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + steerPort;
        steerSocket.setReceiveTimeOut(config.getServerSettings().getServerTimoutMs());
        steerSocket.setSendTimeOut(config.getServerSettings().getServerTimoutMs());
        steerSocket.connect(address);

        utilsSocket = context.createSocket(SocketType.PAIR);
        String address2 = "tcp://" + config.getServerSettings().getServerAddress() + ":" + utilsPort;
        utilsSocket.setReceiveTimeOut(config.getServerSettings().getServerTimoutMs());
        utilsSocket.setSendTimeOut(config.getServerSettings().getServerTimoutMs());
        utilsSocket.connect(address2);
    }

    public void sendSteeringCommand(String command) {
        if(!steerSocket.send(command.getBytes(ZMQ.CHARSET), 0)) checkErrno(steerSocket);
        byte[] reply = steerSocket.recv(0);
        if(reply == null) checkErrno(steerSocket);
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
        if(!utilsSocket.send(command.getBytes(ZMQ.CHARSET), 0)) checkErrno(utilsSocket);
    }
}
