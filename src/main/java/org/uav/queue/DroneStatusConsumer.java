package org.uav.queue;

import org.uav.status.DroneStatus;
import org.uav.parser.DroneStatusMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.List;

public class DroneStatusConsumer {

    private static final String address = "tcp://127.0.0.1:9090";

    private final List<DroneStatus> droneStatuses;
    private final ZMQ.Socket socket;
    private final DroneStatusMessageParser messageParser;
    private final Thread thread;


    public DroneStatusConsumer(ZContext context, List<DroneStatus> droneStatus) {
        this.droneStatuses = droneStatus;
        messageParser = new DroneStatusMessageParser();
        socket = context.createSocket(SocketType.SUB);
        socket.connect(address);
        socket.subscribe("");
        thread = new PositionThread();
    }

    public void start() {
        if(!thread.isAlive())
            thread.start();
    }

    public void stop() {
        thread.interrupt();
    }

    class PositionThread extends Thread {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] reply = socket.recv(0);
                    String message = new String(reply, ZMQ.CHARSET);
                    //System.out.println("Received: [" + message + "]");
                    var size = droneStatuses.size();
                    droneStatuses.addAll(messageParser.parse(message));
                    if (size > 0) {
                        droneStatuses.subList(0, size).clear();
                    }
                    // TODO YOLO
                    droneStatuses.addAll(messageParser.parse(message));

                } catch (ZMQException exception) {
                    System.out.println("Thread " + this.getName() + " has been interrupted");
                    break;
                }
            }
        }
    }
}
