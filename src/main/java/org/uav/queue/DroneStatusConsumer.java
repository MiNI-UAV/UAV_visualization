package org.uav.queue;

import org.uav.config.Configuration;
import org.uav.status.DroneStatus;
import org.uav.parser.DroneStatusMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DroneStatusConsumer {

    private static final String port = "9090";
    private final List<DroneStatus> droneStatuses;
    private final ReentrantLock droneStatusMutex;
    private final ZMQ.Socket socket;
    private final DroneStatusMessageParser messageParser;
    private final Thread thread;


    public DroneStatusConsumer(ZContext context, List<DroneStatus> droneStatus, ReentrantLock droneStatusMutex, Configuration configuration) {
        this.droneStatuses = droneStatus;
        this.droneStatusMutex = droneStatusMutex;
        String address = "tcp://" + configuration.address + ":" + port;
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
                    droneStatusMutex.lock();
                    droneStatuses.clear();
                    droneStatuses.addAll(messageParser.parse(message)); // TODO Message parser should return a map with ids.
                    droneStatusMutex.unlock();

                } catch (ZMQException exception) {
                    System.out.println("Thread " + this.getName() + " has been interrupted");
                    break;
                }
            }
        }
    }
}
