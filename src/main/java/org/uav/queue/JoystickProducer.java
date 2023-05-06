package org.uav.queue;

import org.uav.status.JoystickStatus;
import org.uav.serializer.JoystickMessageSerializer;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JoystickProducer {

    private final ZMQ.Socket socket;
    private final JoystickMessageSerializer messageSerializer;

    public JoystickProducer(ZContext context) {
        messageSerializer = new JoystickMessageSerializer();
        socket = context.createSocket(SocketType.PUB);
        socket.connect("tcp://localhost:10001");
    }

    public void send(JoystickStatus joystickStatus) {
        String message = messageSerializer.serialize(joystickStatus);
        socket.send(message.getBytes(ZMQ.CHARSET), 0);
        System.out.println("Sent: [" + message + "]");
    }
}
