package org.uav.queue;

import org.javatuples.Pair;
import org.uav.config.Config;
import org.uav.model.Notifications;
import org.uav.model.SimulationState;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class NotificationsConsumer extends Thread {
    private final Notifications notifications;
    private final ZMQ.Socket socket;

    public NotificationsConsumer(ZContext context, Config config, SimulationState simulationState) {
        notifications = simulationState.getNotifications();
        String address = "tcp://" + config.serverAddress + ":" + config.ports.notifications;
        socket = context.createSocket(SocketType.SUB);
        socket.connect(address);
        socket.subscribe("");
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] reply = socket.recv(0);
                String message = new String(reply, ZMQ.CHARSET);
                //System.out.println("Received: [" + message + "]");
                parseMessage(message);

            } catch (ZMQException exception) {
                System.out.println("Thread " + this.getName() + " has been interrupted");
                break;
            }
        }
    }

    private void parseMessage(String message) {
        switch(message.charAt(0)) {
            case 't':
                notifications.droneModels = parseDroneModelsMessage(message);
            default:
                break;
        }
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
