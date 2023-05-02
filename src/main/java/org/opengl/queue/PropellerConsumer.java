package org.opengl.queue;

import org.opengl.model.DroneStatus;
import org.opengl.parser.PositionMessageParser;
import org.opengl.parser.PropellerMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class PropellerConsumer {

    private DroneStatus droneStatus;

    private ZContext context;
    private ZMQ.Socket socket;
    private PropellerMessageParser messageParser;
    private Thread thread;


    public PropellerConsumer(ZContext context, DroneStatus droneStatus) {
        this.context = context;
        this.droneStatus = droneStatus;
        messageParser = new PropellerMessageParser();
        socket = context.createSocket(SocketType.SUB);
        socket.connect("tcp://localhost:9090");
        socket.subscribe("om:");
        thread = new PropellerThread();
    }

    public void start() {
        if(!thread.isAlive())
            thread.start();
    }

    class PropellerThread extends Thread {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                String message = new String(reply, ZMQ.CHARSET);
                //System.out.println("Received: [" + message + "]");
                var propellerMessage = messageParser.parse(message);
                droneStatus.propellers = propellerMessage.propellers;
            }
        }
    }
}
