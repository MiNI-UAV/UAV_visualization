package org.uav.queue;

import org.uav.config.Config;
import org.uav.model.Drone;
import org.uav.parser.DroneRequestReplyParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Optional;

public class DroneRequester {
    private static final String TAKEN_MESSAGE = "taken";
    private final Config config;
    private final ZMQ.Socket socket;
    private final ZContext context;
    private final DroneRequestReplyParser messageParser;

    public DroneRequester(ZContext context, Config config) {
        this.context = context;
        this.config = config;
        String address = "tcp://" + config.serverAddress + ":" + config.ports.droneRequester;
        messageParser = new DroneRequestReplyParser();
        socket = context.createSocket(SocketType.REQ);
        socket.connect(address);
    }

    public Optional<Drone> requestNewDrone(String droneName) {
        socket.send(droneName.getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv();
        String message = new String(reply, ZMQ.CHARSET);

        if(message.equals(TAKEN_MESSAGE))
            return Optional.empty();

        DroneRequestReplyMessage parsedMessage = messageParser.parse(message);

        return Optional.of(new Drone(context, parsedMessage.steerPort, parsedMessage.utilsPort, parsedMessage.droneId, config));
    }
}
