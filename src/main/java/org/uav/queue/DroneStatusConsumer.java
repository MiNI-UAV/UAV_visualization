package org.uav.queue;

import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.model.status.DroneStatuses;
import org.uav.model.status.DroneStatus;
import org.uav.parser.DroneStatusMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DroneStatusConsumer {

    private final DroneStatuses droneStatuses;
    private final ReentrantLock droneStatusMutex;
    private final ZMQ.Socket socket;
    private final DroneStatusMessageParser messageParser;
    private final Thread thread;


    public DroneStatusConsumer(ZContext context, SimulationState simulationState, Config config) {
        this.droneStatuses = simulationState.getDroneStatuses();
        this.droneStatusMutex = simulationState.getDroneStatusesMutex();
        String address = "tcp://" + config.serverAddress + ":" + config.ports.droneStatuses;
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
                    droneStatuses.map = messageParser.parse(message).stream()
                            .collect(Collectors.toMap(DroneStatus::getId, Function.identity()));
                    droneStatusMutex.unlock();

                } catch (ZMQException exception) {
                    System.out.println("Thread " + this.getName() + " has been interrupted");
                    break;
                }
            }
        }
    }
}
