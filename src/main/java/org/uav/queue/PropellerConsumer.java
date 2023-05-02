package org.uav.queue;

import org.uav.status.DroneStatus;
import org.uav.parser.PropellerMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

public class PropellerConsumer {

    private final DroneStatus droneStatus;
    private final ZMQ.Socket socket;
    private final PropellerMessageParser messageParser;
    private final Thread thread;


    public PropellerConsumer(ZContext context, DroneStatus droneStatus) {
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

    public void stop() {
        thread.interrupt();
    }

    class PropellerThread extends Thread {
        public void run() throws ZMQException {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] reply = socket.recv(0);
                    String message = new String(reply, ZMQ.CHARSET);
                    //System.out.println("Received: [" + message + "]");
                    var propellerMessage = messageParser.parse(message);
                    droneStatus.propellers = propellerMessage.propellers;
                } catch (ZMQException exception) {
                    System.out.println("Thread " + this.getName() + " has been interrupted");
                    break;
                }
            }
        }
    }
}
