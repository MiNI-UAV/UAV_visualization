package org.uav.logic.communication;

import lombok.Getter;
import org.uav.logic.assets.AvailableControlModes;
import org.uav.logic.config.Config;
import org.uav.logic.state.controlMode.ControlModeDemanded;
import org.uav.logic.state.controlMode.ControlModeReply;
import org.uav.logic.state.simulation.SimulationState;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import zmq.ZError;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DroneCommunication {

    @Getter
    private final int id;
    private final ZMQ.Socket steerSocket;
    private final ZMQ.Socket utilsSocket;
    private final SimulationState simulationState;
    private final AvailableControlModes availableControlModes;

    public DroneCommunication(
            ZContext context,
            int steerPort,
            int utilsPort,
            int droneId,
            SimulationState simulationState,
            Config config,
            AvailableControlModes availableControlModes
    ) {
        this.simulationState = simulationState;
        this.availableControlModes = availableControlModes;
        id = droneId;

        steerSocket = context.createSocket(SocketType.REQ);
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + steerPort;
        steerSocket.setReceiveTimeOut(config.getServerSettings().getDroneTimeoutMs());
        steerSocket.setSendTimeOut(config.getServerSettings().getDroneTimeoutMs());
        steerSocket.connect(address);

        utilsSocket = context.createSocket(SocketType.REQ);
        String address2 = "tcp://" + config.getServerSettings().getServerAddress() + ":" + utilsPort;
        utilsSocket.setReceiveTimeOut(config.getServerSettings().getDroneTimeoutMs());
        utilsSocket.setSendTimeOut(config.getServerSettings().getDroneTimeoutMs());
        utilsSocket.connect(address2);
    }

    public void sendSteeringCommand(String command) {
        try {
            if (!steerSocket.send(command.getBytes(ZMQ.CHARSET), 0)) checkDroneErrno(steerSocket);
            byte[] reply = steerSocket.recv(0);
            if (reply == null) checkDroneErrno(steerSocket);
            String message = new String(reply, ZMQ.CHARSET);
            parseSteeringCommand(message);
        } catch (DroneTimeoutException e) {
            simulationState.setCurrentlyControlledDrone(null);
        }
    }

    private void parseSteeringCommand(String message) {
        int commaIdx = message.indexOf(',');
        commaIdx = commaIdx == -1? message.length(): commaIdx;
        String mode = message.substring(0, commaIdx);
        if (!mode.equals("ok"))
            simulationState.setCurrentControlModeDemanded(parseControlModeMessage(mode, message.substring(commaIdx)));
    }

    private ControlModeDemanded parseControlModeMessage(String mode, String message) {
        Scanner scanner = new Scanner(message);
        scanner.useDelimiter(",");
        if(!availableControlModes.getModes().containsKey(mode))
            return new ControlModeDemanded(mode, new HashMap<>());
        var replyList = availableControlModes.getModes().get(mode).getReply();
        if(replyList == null)
            return new ControlModeDemanded(mode, new HashMap<>());
        Map<ControlModeReply, Float> demanded = replyList.stream()
                .collect(Collectors.toMap(Function.identity(), e -> Float.parseFloat(scanner.next())));
        return new ControlModeDemanded(mode, demanded);
    }

    public String sendUtilsCommand(String command) {
        try {
            if(!utilsSocket.send(command.getBytes(ZMQ.CHARSET), 0)) checkDroneErrno(utilsSocket);
            byte[] reply = utilsSocket.recv(0);
            if(reply == null) checkDroneErrno(utilsSocket);
            return new String(reply, ZMQ.CHARSET);
        } catch (DroneTimeoutException e) {
            simulationState.setCurrentlyControlledDrone(null);
            return "ok;";
        }
    }


    public static void checkDroneErrno(ZMQ.Socket socket) throws DroneTimeoutException {
        if(socket.errno() == ZError.EAGAIN)
            throw new DroneTimeoutException("Drone communication reached timeout.");
    }

    public static class DroneTimeoutException extends Exception {
        public DroneTimeoutException(String message) {
            super(message);
        }
    }
}
