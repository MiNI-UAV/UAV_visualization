package org.opengl.queue;

import org.joml.Vector3f;
import org.opengl.model.DroneStatus;
import org.opengl.parser.MessageParserDroneStatus;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class PositionConsumer {

    private DroneStatus droneStatus;

    private ZContext context;
    private ZMQ.Socket socket;
    private MessageParserDroneStatus messageParser;


    public PositionConsumer(DroneStatus droneStatus) {
        this.droneStatus = droneStatus;
        messageParser = new MessageParserDroneStatus();
        context = new ZContext();
        socket = context. createSocket(SocketType.SUB);
        socket.connect("tcp://localhost:5555");
        socket.subscribe("pos:");
        var thread = new PositionThread();
        thread.start();
    }

    class PositionThread extends Thread {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                String message = new String(reply, ZMQ.CHARSET);
                System.out.println("Received: [" + message + "]");
                //String response = "Hello, world!";
                //socket.send(response.getBytes(ZMQ.CHARSET), 0);
                var newStatus = messageParser.parse(message);
                droneStatus.position = newStatus.position;
                droneStatus.rotation = newStatus.rotation;
            }
        }
    }
}
