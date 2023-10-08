package org.uav.queue;

import org.uav.config.AvailableControlModes;
import org.uav.config.Config;
import org.uav.model.Drone;
import org.uav.model.SimulationState;
import org.uav.parser.DroneRequestReplyParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.uav.utils.ZmqUtils.checkErrno;

public class DroneRequester {
    private final Config config;
    private final SimulationState simulationState;
    private final AvailableControlModes availableControlModes;
    private final ZMQ.Socket socket;
    private final ZContext context;
    private final DroneRequestReplyParser messageParser;

    public DroneRequester(ZContext context, SimulationState simulationState, Config config, AvailableControlModes availableControlModes) {
        this.simulationState = simulationState;
        this.availableControlModes = availableControlModes;
        this.context = context;
        this.config = config;
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + config.getPorts().getDroneRequester();
        messageParser = new DroneRequestReplyParser();
        socket = context.createSocket(SocketType.REQ);
        socket.setSendTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.setReceiveTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.connect(address);
    }

    public Optional<Drone> requestNewDrone(String droneName, String configNameHash) {
        if(!socket.send(("s:" + droneName + ";" + configNameHash).getBytes(ZMQ.CHARSET), 0)) checkErrno(socket);
        byte[] reply = socket.recv();
        String message = new String(reply, ZMQ.CHARSET);

        if(!parseReply(message))
            return Optional.empty();

        DroneRequestReplyMessage parsedMessage = messageParser.parse(message);

        return Optional.of(new Drone(context, parsedMessage.steerPort, parsedMessage.utilsPort, parsedMessage.droneId, simulationState, config, availableControlModes));
    }

    public boolean parseReply(String reply)
    {
        if(reply.equals("-1"))
        {
            //Invalid drone name
            return false;
        }
        if(reply.equals("-2"))
        {
            //Unknown config file
            return false;
        }
        return true;
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
}
