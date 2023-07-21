package org.uav.processor;

import org.uav.config.Config;
import org.uav.input.InputHandler;
import org.uav.model.SimulationState;
import org.uav.queue.DroneRequester;
import org.uav.queue.DroneStatusConsumer;
import org.uav.queue.HeartbeatProducer;
import org.uav.queue.ProjectileStatusesConsumer;
import org.zeromq.ZContext;

public class SimulationStateProcessor implements AutoCloseable {


    private final SimulationState simulationState;
    private final Config config;
    private final DroneRequester droneRequester;
    private final DroneStatusConsumer droneStatusConsumer;
    private final ProjectileStatusesConsumer projectileStatusesConsumer;
    private final HeartbeatProducer heartbeatProducer;
    private final InputHandler inputHandler;


    public SimulationStateProcessor(SimulationState simulationState, Config config) {
        this.simulationState = simulationState;
        this.config = config;
        var context = new ZContext();
        droneRequester = new DroneRequester(context, config);
        droneStatusConsumer = new DroneStatusConsumer(context, simulationState, config);
        projectileStatusesConsumer = new ProjectileStatusesConsumer(context, simulationState, config);
        heartbeatProducer = new HeartbeatProducer(config);
        inputHandler = new InputHandler(simulationState, config);
    }

    public void openCommunication() {
        droneStatusConsumer.start();
        projectileStatusesConsumer.start();
    }

    public void requestNewDrone() {
        var newDroneResult = droneRequester.requestNewDrone(config.droneName);
        simulationState.setCurrentlyControlledDrone(newDroneResult.orElseThrow());
    }

    public void update() {
        heartbeatProducer.sustainHeartBeat(simulationState.getCurrentlyControlledDrone());
        inputHandler.handleInput(simulationState.getWindow());
        simulationState.getCamera().updateCamera();
        updateCurrentEntityStatuses();
    }

    private void updateCurrentEntityStatuses() {
        simulationState.getDroneStatusesMutex().lock();
        simulationState.getCurrPassDroneStatuses().map = simulationState.getDroneStatuses().map;
        simulationState.getDroneStatusesMutex().unlock();
        simulationState.getProjectileStatusesMutex().lock();
        simulationState.getCurrPassProjectileStatuses().map = simulationState.getProjectileStatuses().map;
        simulationState.getProjectileStatusesMutex().unlock();
    }

    @Override
    public void close() {
        droneStatusConsumer.stop();
        projectileStatusesConsumer.stop();
    }
}
