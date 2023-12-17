package org.uav.logic.communication;

import org.uav.logic.assets.AvailableControlModes;
import org.uav.logic.config.Config;
import org.uav.logic.state.simulation.SimulationState;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

import static org.uav.utils.ZmqUtils.checkErrno;

public class DroneRequester {
    private final Config config;
    private final SimulationState simulationState;
    private final AvailableControlModes availableControlModes;
    private final ZMQ.Socket socket;
    private final ZContext context;

    public DroneRequester(ZContext context, SimulationState simulationState, Config config, AvailableControlModes availableControlModes) {
        this.simulationState = simulationState;
        this.availableControlModes = availableControlModes;
        this.context = context;
        this.config = config;
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + config.getPorts().getDroneRequester();
        socket = context.createSocket(SocketType.REQ);
        socket.setSendTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.setReceiveTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.connect(address);
    }

    public Optional<DroneCommunication> requestNewDrone(String droneName, String configNameHash) {
        if(!socket.send(("s:" + droneName + ";" + configNameHash).getBytes(ZMQ.CHARSET), 0)) checkErrno(socket);
        byte[] reply = socket.recv();
        String message = new String(reply, ZMQ.CHARSET);

        if(!parseReply(message))
            return Optional.empty();

        DroneRequestReplyMessage parsedMessage = parse(message);

        return Optional.of(new DroneCommunication(context, parsedMessage.steerPort, parsedMessage.utilsPort, parsedMessage.droneId, simulationState, config, availableControlModes));
    }

    public boolean parseReply(String reply)
    {
        //Invalid drone name or Unknown config file
        return !reply.equals("-1") && !reply.equals("-2");
    }

    public String sendConfigFile(String configPath)
    {
        Path fileName = Path.of(configPath);
        String configContent;
        try {
            configContent = Files.readString(fileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read drone model config file");
        }

        String sb = "c:" + configContent;
        if(!socket.send(sb.getBytes(ZMQ.CHARSET), 0)) checkErrno(socket);
        byte[] reply = socket.recv();
        if(reply == null) checkErrno(socket);
        String message = new String(reply, ZMQ.CHARSET);
        return message.substring(message.lastIndexOf(';') + 1);
    }


    private DroneRequestReplyMessage parse(String input) {
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter(",");
        int droneId = Integer.parseInt(scanner.next());
        int steerPort = Integer.parseInt(scanner.next());
        int utilsPort = Integer.parseInt(scanner.next());
        return new DroneRequestReplyMessage(droneId, steerPort, utilsPort);
    }

    private static class DroneRequestReplyMessage {
        int droneId;
        int steerPort;
        int utilsPort;

        public DroneRequestReplyMessage(int droneId, int steerPort, int utilsPort) {
            this.droneId = droneId;
            this.steerPort = steerPort;
            this.utilsPort = utilsPort;
        }
    }
}
