package org.uav.logic.state.simulation;

import org.lwjgl.glfw.GLFW;
import org.uav.logic.assets.AvailableControlModes;
import org.uav.logic.communication.*;
import org.uav.logic.config.Config;
import org.uav.logic.messages.MessageBoard;
import org.uav.logic.state.projectile.Projectile;
import org.uav.presentation.entity.drone.DroneState;
import org.zeromq.ZContext;

import java.nio.file.Paths;

public class SimulationStateProcessor implements AutoCloseable {

    private static final String KILL_COMMAND = "kill";
    private final SimulationState simulationState;
    private final Config config;
    private final DroneRequester droneRequester;
    private final DroneStatusConsumer droneStatusConsumer;
    private final ProjectileStatusesConsumer projectileStatusesConsumer;
    private final NotificationsConsumer notificationsConsumer;


    public SimulationStateProcessor(ZContext context, SimulationState simulationState, Config config, AvailableControlModes availableControlModes, MessageBoard messageBoard) {
        this.simulationState = simulationState;
        this.config = config;
        droneRequester = new DroneRequester(context, simulationState, config, availableControlModes);
        droneStatusConsumer = new DroneStatusConsumer(context, simulationState, config);
        projectileStatusesConsumer = new ProjectileStatusesConsumer(context, simulationState, config);
        notificationsConsumer = new NotificationsConsumer(context, config, simulationState);
        notificationsConsumer.subscribe(messageBoard.produceSubscriber());
    }

    public void openCommunication() {
        droneStatusConsumer.start();
        projectileStatusesConsumer.start();
        notificationsConsumer.start();
    }

    public void requestNewDrone() {
        var newDroneResult = droneRequester.requestNewDrone(config.getDroneSettings().getDroneName(), simulationState.getDroneModelChecksum());
        simulationState.setCurrentlyControlledDrone(newDroneResult.orElseThrow());
    }

    public void updateSimulationState() {
        simulationState.getDroneStatusesMutex().lock();
        var droneModels = simulationState.getNotifications().droneModelsNames;
        var previousDrones = simulationState.getDronesInAir().keySet().stream().toList();
        for(int key : previousDrones)
            if (!simulationState.getDroneStatuses().map.containsKey(key)) simulationState.getDronesInAir().remove(key);
        for(var status : simulationState.getDroneStatuses().map.values()) {
            simulationState.getDronesInAir().computeIfPresent(status.id, (id, droneState) -> droneState.update(status, droneModels));
            simulationState.getDronesInAir().putIfAbsent(status.id, new DroneState(status));
        }
        simulationState.getDroneStatusesMutex().unlock();

        simulationState.getProjectileStatusesMutex().lock();
        simulationState.getCurrPassProjectileStatuses().map = simulationState.getProjectileStatuses().map;
        simulationState.getProjectileStatusesMutex().unlock();

        simulationState.getAmmos().forEach(Projectile::update);
        simulationState.getCargos().forEach(Projectile::update);

        simulationState.setSimulationTimeS((float) GLFW.glfwGetTime());
    }

    @Override
    public void close() {
        droneStatusConsumer.stop();
        projectileStatusesConsumer.stop();
        notificationsConsumer.interrupt();
    }

    public void respawnDrone() {
        DroneCommunication oldDrone = simulationState.getCurrentlyControlledDrone();
        requestNewDrone();
        oldDrone.sendUtilsCommand(KILL_COMMAND);
        simulationState.setCurrentControlModeDemanded(null);
        simulationState.getCargos().forEach(Projectile::reset);
        simulationState.getAmmos().forEach(Projectile::reset);
    }

    public void saveDroneModelChecksum(String droneConfig) {
        String droneModelConfigPath = Paths.get(System.getProperty("user.dir"), "drones", droneConfig).toString();
        String droneModelChecksum = droneRequester.sendConfigFile(droneModelConfigPath);
        simulationState.setDroneModelChecksum(droneModelChecksum);
    }

    public void updateCamera() {
        simulationState.getCamera().updateCamera();
    }

    public void nextFrame() {
        simulationState.getFpsCounter().nextFrame();
    }
}
