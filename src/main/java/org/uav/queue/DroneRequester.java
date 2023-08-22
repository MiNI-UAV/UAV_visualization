package org.uav.queue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.uav.config.Config;
import org.uav.model.Drone;
import org.uav.model.ServerInfo;
import org.uav.model.SimulationState;
import org.uav.parser.DroneRequestReplyParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

public class DroneRequester {
    private final Config config;
    private final SimulationState simulationState;
    private final ZMQ.Socket socket;
    private final ZContext context;
    private final DroneRequestReplyParser messageParser;

    public DroneRequester(ZContext context, SimulationState simulationState, Config config) {
        this.simulationState = simulationState;
        this.context = context;
        this.config = config;
        String address = "tcp://" + config.serverAddress + ":" + config.ports.droneRequester;
        messageParser = new DroneRequestReplyParser();
        socket = context.createSocket(SocketType.REQ);
        socket.connect(address);
    }

    public Optional<Drone> requestNewDrone(String droneName, String configNameHash) {
        socket.send(("s:" + droneName + ";" + configNameHash).getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv();
        String message = new String(reply, ZMQ.CHARSET);

        if(!parseReply(message))
            return Optional.empty();

        DroneRequestReplyMessage parsedMessage = messageParser.parse(message);

        return Optional.of(new Drone(context, parsedMessage.steerPort, parsedMessage.utilsPort, parsedMessage.droneId, simulationState, config));
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

    public ServerInfo fetchServerInfo() {
        socket.send(("i").getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv();
        String message = new String(reply, ZMQ.CHARSET);

        JSONObject obj = new JSONObject(message);
        String assetChecksum = obj.getString("checksum");
        String serverMap = obj.getString("map");
        var configs = new ArrayList<String>();
        JSONArray arr = obj.getJSONArray("configs");
        for (int i = 0; i < arr.length(); i++)
            configs.add(arr.getString(i));

        return new ServerInfo(assetChecksum, serverMap, configs);
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
        socket.send(sb.getBytes(ZMQ.CHARSET), 0);
        byte[] reply = socket.recv();
        String message = new String(reply, ZMQ.CHARSET);
        return message.substring(message.lastIndexOf(';') + 1);
    }
}
