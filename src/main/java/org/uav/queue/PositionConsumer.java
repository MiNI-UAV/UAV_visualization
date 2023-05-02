package org.uav.queue;

import org.uav.status.DroneStatus;
import org.uav.parser.PositionMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class PositionConsumer {

    private DroneStatus droneStatus;

    private ZContext context;
    private ZMQ.Socket socket;
    private PositionMessageParser messageParser;
    private Thread thread;


    public PositionConsumer(ZContext context, DroneStatus droneStatus) {
        this.context = context;
        this.droneStatus = droneStatus;
        messageParser = new PositionMessageParser();
        context = new ZContext();
                //String response = "Hello, world!";
                //socket.send(response.getBytes(ZMQ.CHARSET), 0);
        socket = context.createSocket(SocketType.SUB);
        socket.connect("tcp://localhost:9090");
        socket.subscribe("pos:");
        thread = new PositionThread();
    }

    public void start() {
        if(!thread.isAlive())
            thread.start();
    }

    class PositionThread extends Thread {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                String message = new String(reply, ZMQ.CHARSET);
                //System.out.println("Received: [" + message + "]");
                var newStatus = messageParser.parse(message);
                droneStatus.position = newStatus.position;
                droneStatus.rotation = newStatus.rotation;
            }
        }
    }
}
