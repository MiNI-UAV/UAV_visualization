package org.uav.logic.communication;

import org.joml.Vector3f;
import org.uav.logic.config.Config;
import org.uav.logic.state.projectile.ProjectileStatus;
import org.uav.logic.state.projectile.ProjectileStatuses;
import org.uav.logic.state.simulation.SimulationState;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.uav.utils.ZmqUtils.checkErrno;

public class ProjectileStatusesConsumer {
    private final ProjectileStatuses projectileStatuses;
    private final ReentrantLock projectileStatusesMutex;
    private final ZMQ.Socket socket;
    private final Thread thread;


    public ProjectileStatusesConsumer(ZContext context, SimulationState simulationState, Config config) {
        this.projectileStatuses = simulationState.getProjectileStatuses();
        this.projectileStatusesMutex = simulationState.getProjectileStatusesMutex();
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + config.getPorts().getProjectileStatuses();
        socket = context.createSocket(SocketType.SUB);
        socket.setSendTimeOut(config.getServerSettings().getServerTimeoutMs());
        socket.setReceiveTimeOut(config.getServerSettings().getServerTimeoutMs());
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
                    projectileStatusesMutex.lock();
                    projectileStatuses.map = parse(message).stream()
                            .collect(Collectors.toMap(projectile -> projectile.id, Function.identity()));
                    projectileStatusesMutex.unlock();

                } catch (ZMQException exception) {
                    break;
                }
            }
        }
    }

    private List<ProjectileStatus> parse(String input) {
        return Arrays.stream(input.split(";")).skip(1).map(this::toProjectile).toList();
    }

    private ProjectileStatus toProjectile(String input) {
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter(",");

        return new ProjectileStatus(
                Integer.parseInt(scanner.next()),
                new Vector3f(
                        Float.parseFloat(scanner.next()),
                        Float.parseFloat(scanner.next()),
                        Float.parseFloat(scanner.next())),
                new Vector3f(
                        Float.parseFloat(scanner.next()),
                        Float.parseFloat(scanner.next()),
                        Float.parseFloat(scanner.next())
                )
        );
    }
}
