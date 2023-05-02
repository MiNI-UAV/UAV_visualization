package org.uav.queue;

import org.uav.status.DroneStatus;
import org.uav.parser.PositionMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class PositionConsumer {

    private final DroneStatus droneStatus;
    private final ZMQ.Socket socket;
    private final PositionMessageParser messageParser;
    private final Thread thread;


    public PositionConsumer(ZContext context, DroneStatus droneStatus) {
        this.droneStatus = droneStatus;
        messageParser = new PositionMessageParser();
        socket = context.createSocket(SocketType.SUB);
        socket.connect("tcp://localhost:9090");
        socket.subscribe("pos:");
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
                    var newStatus = messageParser.parse(message);
                    droneStatus.position = newStatus.position;
                    droneStatus.rotation = newStatus.rotation;
                } catch (ZMQException exception) {
                    System.out.println("Thread " + this.getName() + " has been interrupted");
                    break;
                }
            }
        }
    }
}
