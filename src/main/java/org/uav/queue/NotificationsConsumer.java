package org.uav.queue;

import org.javatuples.Pair;
import org.joml.Vector3f;
import org.uav.config.Config;
import org.uav.model.Notifications;
import org.uav.model.SimulationState;
import org.uav.model.rope.Rope;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.*;
import java.util.stream.Collectors;

import static org.uav.utils.ZmqUtils.checkErrno;

public class NotificationsConsumer extends Thread {
    private final Notifications notifications;
    private final ZMQ.Socket socket;

    public NotificationsConsumer(ZContext context, Config config, SimulationState simulationState) {
        notifications = simulationState.getNotifications();
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + config.getPorts().getNotifications();
        socket = context.createSocket(SocketType.SUB);
        socket.setSendTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.setReceiveTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.connect(address);
        socket.subscribe("");
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] reply = socket.recv(0);
                if(reply == null) checkErrno(socket);
                String message = new String(reply, ZMQ.CHARSET);
                //System.out.println("Received: [" + message + "]");
                parseMessage(message);

            } catch (ZMQException exception) {
                break;
            }
        }
    }

    private void parseMessage(String message) {
        switch(message.charAt(0)) {
            case 't' -> notifications.droneModels = parseDroneModelsMessage(message);
            case 'o' -> {}
            case 'l' -> notifications.ropes = parseRopeMessage(message);
            default -> {}
        }
    }

    private List<Rope> parseRopeMessage(String message) {
        message = message.substring(2);
        if(message.isEmpty()) return new ArrayList<>();
        return Arrays.stream(message.split(";")).map((String input) -> {
            Scanner scanner = new Scanner(input);
            scanner.useDelimiter(",");
            int ownerId = Integer.parseInt(scanner.next());
            int objectId = Integer.parseInt(scanner.next());
            float ropeLength = Float.parseFloat(scanner.next());
            float xOffset = Float.parseFloat(scanner.next());
            float yOffset = Float.parseFloat(scanner.next());
            float zOffset = Float.parseFloat(scanner.next());
            var ownerOffset = new Vector3f(xOffset, yOffset, zOffset);
            return new Rope(ropeLength, ownerId, objectId, ownerOffset);
        }).toList();
    }

    private Map<Integer, String> parseDroneModelsMessage(String message) {
        message = message.substring(2);
        return Arrays.stream(message.split(";")).map((String input) -> {
            Scanner scanner = new Scanner(input);
            scanner.useDelimiter(",");
            int droneId = Integer.parseInt(scanner.next());
            String droneModelName = scanner.next();
            return new Pair<>(droneId, droneModelName);
        }).collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
    }
}
