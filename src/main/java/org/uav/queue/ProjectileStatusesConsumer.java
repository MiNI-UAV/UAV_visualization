package org.uav.queue;

import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.model.status.ProjectileStatuses;
import org.uav.parser.ProjectileStatusMessageParser;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.uav.utils.ZmqUtils.checkErrno;

public class ProjectileStatusesConsumer {
    private final ProjectileStatuses projectileStatuses;
    private final ReentrantLock projectileStatusesMutex;
    private final ZMQ.Socket socket;
    private final ProjectileStatusMessageParser messageParser;
    private final Thread thread;


    public ProjectileStatusesConsumer(ZContext context, SimulationState simulationState, Config config) {
        this.projectileStatuses = simulationState.getProjectileStatuses();
        this.projectileStatusesMutex = simulationState.getProjectileStatusesMutex();
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + config.getPorts().getProjectileStatuses();
        messageParser = new ProjectileStatusMessageParser();
        socket = context.createSocket(SocketType.SUB);
        socket.setSendTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.setReceiveTimeOut(config.getServerSettings().getServerTimoutMs());
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
                    if(reply == null) checkErrno(socket);
                    String message = new String(reply, ZMQ.CHARSET);
                    //System.out.println("Received: [" + message + "]");
                    projectileStatusesMutex.lock();
                    projectileStatuses.map = messageParser.parse(message).stream()
                            .collect(Collectors.toMap(projectile -> projectile.id, Function.identity()));
                    projectileStatusesMutex.unlock();

                } catch (ZMQException exception) {
                    break;
                }
            }
        }
    }
}
