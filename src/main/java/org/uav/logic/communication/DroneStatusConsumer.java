package org.uav.logic.communication;

import org.uav.logic.config.Config;
import org.uav.logic.state.drone.DroneStatus;
import org.uav.logic.state.drone.DroneStatuses;
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

public class DroneStatusConsumer {

    private final DroneStatuses droneStatuses;
    private final ReentrantLock droneStatusMutex;
    private final ZMQ.Socket socket;
    private final Thread thread;


    public DroneStatusConsumer(ZContext context, SimulationState simulationState, Config config) {
        this.droneStatuses = simulationState.getDroneStatuses();
        this.droneStatusMutex = simulationState.getDroneStatusesMutex();
        String address = "tcp://" + config.getServerSettings().getServerAddress() + ":" + config.getPorts().getDroneStatuses();
        socket = context.createSocket(SocketType.SUB);
        socket.setSendTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.setReceiveTimeOut(config.getServerSettings().getServerTimoutMs());
        socket.connect(address);
        socket.subscribe("");
        thread = new PositionThread();
    }

    public void start() {
        if(!thread.isAlive())
            thread.start();
    }

    public void stop() {
        thread.interrupt();
    }

    class PositionThread extends Thread {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] reply = socket.recv(0);
                    if(reply == null) checkErrno(socket);
                    String message = new String(reply, ZMQ.CHARSET);
                    //System.out.println("Received: [" + message + "]");
                    droneStatusMutex.lock();
                    droneStatuses.map = parse(message).stream()
                            .collect(Collectors.toMap(drone -> drone.id, Function.identity()));
                    droneStatusMutex.unlock();

                } catch (ZMQException exception) {
                    break;
                }
            }
        }
    }

    private List<DroneStatus> parse(String input) {
        return Arrays.stream(input.split(";")).map(this::toDrone).toList();
    }

    private DroneStatus toDrone(String input) {
        var drone = new DroneStatus();
        Scanner scanner = new Scanner(input);
        scanner.useDelimiter(",");

        drone.id = Integer.parseInt(scanner.next());

        drone.time = Float.parseFloat(scanner.next());

        drone.position.x = Float.parseFloat(scanner.next());
        drone.position.y = Float.parseFloat(scanner.next());
        drone.position.z = Float.parseFloat(scanner.next());

        drone.rotation.w = Float.parseFloat(scanner.next());
        drone.rotation.x = Float.parseFloat(scanner.next());
        drone.rotation.y = Float.parseFloat(scanner.next());
        drone.rotation.z = Float.parseFloat(scanner.next());

        drone.linearVelocity.x = Float.parseFloat(scanner.next());
        drone.linearVelocity.y = Float.parseFloat(scanner.next());
        drone.linearVelocity.z = Float.parseFloat(scanner.next());

        drone.angularVelocity.x = Float.parseFloat(scanner.next());
        drone.angularVelocity.y = Float.parseFloat(scanner.next());
        drone.angularVelocity.z = Float.parseFloat(scanner.next());

        while (scanner.hasNext()){
            drone.propellersRadps.add(Float.parseFloat(scanner.next()));
        }

        return drone;
    }
}
