package org.uav.utils;

import org.zeromq.ZMQ;
import zmq.ZError;

public class ZmqUtils {
    public static void checkErrno(ZMQ.Socket socket) {
        if(socket.errno() == ZError.EAGAIN)
            throw new RuntimeException("Failed to reach reach the server");
    }
}
