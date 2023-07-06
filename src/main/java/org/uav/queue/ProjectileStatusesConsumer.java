package org.uav.queue;

import org.uav.config.Configuration;
import org.uav.model.status.ProjectileStatus;
import org.uav.model.status.ProjectileStatuses;
import org.uav.parser.ProjectileStatusMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProjectileStatusesConsumer {
    private static final String port = "9100";
    private final ProjectileStatuses projectileStatuses;
    private final ReentrantLock projectileStatusesMutex;
    private final ZMQ.Socket socket;
    private final ProjectileStatusMessageParser messageParser;
    private final Thread thread;


    public ProjectileStatusesConsumer(ZContext context, ProjectileStatuses projectileStatuses, ReentrantLock projectileStatusesMutex, Configuration configuration) {
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
                    projectileStatuses.map.clear();
                    projectileStatuses.map = messageParser.parse(message).stream()
                            .collect(Collectors.toMap(ProjectileStatus::getId, Function.identity()));
                    projectileStatusesMutex.unlock();

                } catch (ZMQException exception) {
                    System.out.println("Thread " + this.getName() + " has been interrupted");
                    break;
                }
            }
        }
    }
}
