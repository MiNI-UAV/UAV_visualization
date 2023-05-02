package org.opengl.queue;

import org.opengl.model.DroneStatus;
import org.opengl.model.JoystickStatus;
import org.opengl.parser.PositionMessageParser;
import org.opengl.serializer.JoystickMessageSerializer;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JoystickProducer {

    private ZContext context;
    private ZMQ.Socket socket;
    private JoystickMessageSerializer messageSerializer;
    private Thread thread;


    public JoystickProducer(ZContext context) {
        this.context = context;
        messageSerializer = new JoystickMessageSerializer();
        context = new ZContext();
        socket = context.createSocket(SocketType.PUB);
        socket.connect("tcp://localhost:10001");
    }

    public void send(JoystickStatus joystickStatus) {
        String message = messageSerializer.serialize(joystickStatus);
        socket.send(message.getBytes(ZMQ.CHARSET), 0);
        System.out.println("Sent: [" + message + "]");
    }
}
