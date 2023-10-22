package org.uav.processor;

import org.lwjgl.glfw.GLFW;
import org.uav.config.AvailableControlModes;
import org.uav.config.Config;
import org.uav.model.Drone;
import org.uav.model.Projectile;
import org.uav.model.SimulationState;
import org.uav.queue.DroneRequester;
import org.uav.queue.DroneStatusConsumer;
import org.uav.queue.NotificationsConsumer;
import org.uav.queue.ProjectileStatusesConsumer;
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


    public SimulationStateProcessor(ZContext context, SimulationState simulationState, Config config, AvailableControlModes availableControlModes) {
        this.simulationState = simulationState;
        this.config = config;
        droneRequester = new DroneRequester(context, simulationState, config, availableControlModes);
        droneStatusConsumer = new DroneStatusConsumer(context, simulationState, config);
        projectileStatusesConsumer = new ProjectileStatusesConsumer(context, simulationState, config);
        notificationsConsumer = new NotificationsConsumer(context, config, simulationState);
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
        simulationState.getCurrPassDroneStatuses().map = simulationState.getDroneStatuses().map;
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
        Drone oldDrone = simulationState.getCurrentlyControlledDrone();
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
}
