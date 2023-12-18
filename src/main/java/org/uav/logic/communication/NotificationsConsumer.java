package org.uav.logic.communication;

import org.javatuples.Pair;
import org.joml.Vector3f;
import org.uav.logic.config.Config;
import org.uav.logic.messages.Message;
import org.uav.logic.messages.Publisher;
import org.uav.logic.state.notifications.Notifications;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.rope.Rope;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.uav.utils.ZmqUtils.checkErrno;

public class NotificationsConsumer extends Thread implements Publisher {
    private final SimulationState simulationState;
    private final Notifications notifications;
    private final ZMQ.Socket socket;
    private final List<Consumer<Message>> subscribers;

    public NotificationsConsumer(ZContext context, Config config, SimulationState simulationState) {
        this.simulationState = simulationState;
        subscribers = new ArrayList<>();
        notifications = simulationState.getNotifications();
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + config.getPorts().getNotifications();
        socket = context.createSocket(SocketType.SUB);
        socket.setSendTimeOut(config.getServerSettings().getServerTimeoutMs());
        socket.setReceiveTimeOut(config.getServerSettings().getServerTimeoutMs());
        socket.connect(address);
        socket.subscribe("");
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] reply = socket.recv(0);
                if(reply == null) checkErrno(socket);
                String message = new String(reply, ZMQ.CHARSET);
                parseMessage(message);

            } catch (ZMQException exception) {
                break;
            }
        }
    }

    private void parseMessage(String message) {
        switch(message.charAt(0)) {
            case 't' -> notifications.droneModelsNames = parseModelMapMessage(message);
            case 'o' -> notifications.projectileModelsNames = parseModelMapMessage(message);
            case 'l' -> notifications.ropes = parseRopeMessage(message);
            case 'p' -> notifySubscriberOfServerNotification(message);
            default -> {}
        }
    }

    private void notifySubscriberOfServerNotification(String message) {
        var drone = simulationState.getPlayerDrone();
        if(drone.isEmpty()) return;
        Message notification = parseServerNotification(message, drone.get().droneStatus.id);
        if(notification != null)
            notifySubscriber(notification);
    }

    private Message parseServerNotification(String message, int drone) {
        message = message.substring(2);
        if(message.isEmpty()) return null;
        Scanner scanner = new Scanner(message.split(";")[0]);
        scanner.useDelimiter(",");
        int target = Integer.parseInt(scanner.next());
        if(target >= 0 && target != drone) return null;
        String category = scanner.next();
        String colorString = scanner.next();
        Color color = new Color(
                Integer.valueOf( colorString.substring(0, 2), 16),
                Integer.valueOf( colorString.substring( 2, 4 ), 16 ),
                Integer.valueOf( colorString.substring( 4, 6 ), 16 )
        );
        float showTime = Float.parseFloat(scanner.next()) / 1000;
        String content = scanner.next();
        return new Message(content, category, showTime, color, true);
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

    private Map<Integer, String> parseModelMapMessage(String message) {
        message = message.substring(2);
        if(message.isEmpty()) return new HashMap<>();
        return Arrays.stream(message.split(";")).map((String input) -> {
            Scanner scanner = new Scanner(input);
            scanner.useDelimiter(",");
            int droneId = Integer.parseInt(scanner.next());
            String droneModelName = scanner.next();
            return new Pair<>(droneId, droneModelName);
        }).collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
    }

    @Override
    public List<Consumer<Message>> getSubscribers() {
        return subscribers;
    }
}
