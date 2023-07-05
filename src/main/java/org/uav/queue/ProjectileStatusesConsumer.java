package org.uav.queue;

import org.uav.config.Configuration;
import org.uav.model.ProjectileStatus;
import org.uav.parser.ProjectileStatusMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ProjectileStatusesConsumer {
    private static final String port = "9100";
    private final List<ProjectileStatus> projectileStatuses;
    private final ReentrantLock projectileStatusesMutex;
    private final ZMQ.Socket socket;
    private final ProjectileStatusMessageParser messageParser;
    private final Thread thread;


    public ProjectileStatusesConsumer(ZContext context, List<ProjectileStatus> projectileStatuses, ReentrantLock projectileStatusesMutex, Configuration configuration) {
        this.projectileStatuses = projectileStatuses;
        this.projectileStatusesMutex = projectileStatusesMutex;
        String address = "tcp://" + configuration.address + ":" + port;
        messageParser = new ProjectileStatusMessageParser();
        socket = context.createSocket(SocketType.SUB);
        socket.connect(address);
        socket.subscribe("");
        thread = new ProjectileStatusesThread();
    }

    public void start() {
        if(!thread.isAlive())
            thread.start();
    }

    public void stop() {
        thread.interrupt();
    }

    class ProjectileStatusesThread extends Thread {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] reply = socket.recv(0);
                    String message = new String(reply, ZMQ.CHARSET);
                    //System.out.println("Received: [" + message + "]");
                    projectileStatusesMutex.lock();
                    projectileStatuses.clear();
                    projectileStatuses.addAll(messageParser.parse(message)); // TODO Message parser should return a map with ids.
                    projectileStatusesMutex.unlock();

                } catch (ZMQException exception) {
                    System.out.println("Thread " + this.getName() + " has been interrupted");
                    break;
                }
            }
        }
    }
}
