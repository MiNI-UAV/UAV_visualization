package org.uav.queue;

import org.uav.config.Config;
import org.uav.model.Drone;
import org.uav.parser.DroneRequestReplyParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Optional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DroneRequester {
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

    public Optional<Drone> requestNewDrone(String droneName)
    {
        return requestNewDrone(droneName, "config");
    }

    public Optional<Drone> requestNewDrone(String droneName, String configName) {
        socket.send(("s:" + droneName + ";" + configName).getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv();
        String message = new String(reply, ZMQ.CHARSET);

        if(parseReply(message))
            return Optional.empty();

        DroneRequestReplyMessage parsedMessage = messageParser.parse(message);

        return Optional.of(new Drone(context, parsedMessage.steerPort, parsedMessage.utilsPort, parsedMessage.droneId, config));
    }

    public boolean parseReply(String reply)
    {
        if(reply.equals("-1"))
        {
            //Invalid drone name
            return true;
        }
        if(reply.equals("-2"))
        {
            //Unknown config file
            return true;
        }
        return false;
    }

    public void sendConfigFile(String configName, String configPath)
    {
        Path fileName
            = Path.of(configPath);
        String configContent;
        try {
            configContent = Files.readString(fileName);
        } catch (IOException e) {
            // TODO: handle exception
            return;
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("c:");
        sb.append(configName);
        sb.append(";");
        sb.append(configContent);
        socket.send(sb.toString().getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv();
        String message = new String(reply, ZMQ.CHARSET);
        parseReply(message);
    }
}
