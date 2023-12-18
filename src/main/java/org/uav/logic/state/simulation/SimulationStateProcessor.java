package org.uav.logic.state.simulation;

import org.lwjgl.glfw.GLFW;
import org.uav.logic.assets.AvailableControlModes;
import org.uav.logic.communication.*;
import org.uav.logic.config.Config;
import org.uav.logic.messages.Message;
import org.uav.logic.messages.MessageBoard;
import org.uav.logic.messages.Publisher;
import org.uav.logic.state.projectile.Projectile;
import org.uav.presentation.entity.drone.DroneState;
import org.zeromq.ZContext;

import java.awt.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SimulationStateProcessor implements AutoCloseable, Publisher {

    private static final String KILL_COMMAND = "kill";
    private final SimulationState simulationState;
    private final Config config;
    private final DroneRequester droneRequester;
    private final DroneStatusConsumer droneStatusConsumer;
    private final ProjectileStatusesConsumer projectileStatusesConsumer;
    private final NotificationsConsumer notificationsConsumer;
    private final List<Consumer<Message>> subscribers;


    public SimulationStateProcessor(ZContext context, SimulationState simulationState, Config config, AvailableControlModes availableControlModes, MessageBoard messageBoard) {
        this.simulationState = simulationState;
        this.config = config;
        droneRequester = new DroneRequester(context, simulationState, config, availableControlModes);
        droneStatusConsumer = new DroneStatusConsumer(context, simulationState, config);
        projectileStatusesConsumer = new ProjectileStatusesConsumer(context, simulationState, config);
        notificationsConsumer = new NotificationsConsumer(context, config, simulationState);
        notificationsConsumer.subscribe(messageBoard.produceSubscriber());
        subscribers = new ArrayList<>();
        subscribe(messageBoard.produceSubscriber());
    }

    public void openCommunication() {
        droneStatusConsumer.start();
        projectileStatusesConsumer.start();
        notificationsConsumer.start();
    }

    public Optional<DroneCommunication> requestNewDrone() {
        return droneRequester.requestNewDrone(config.getDroneSettings().getDroneName(), simulationState.getDroneModelChecksum());
    }

    public void updateSimulationState() {
        updateDronesInAir();

        simulationState.getProjectileStatusesMutex().lock();
        simulationState.getCurrPassProjectileStatuses().map = simulationState.getProjectileStatuses().map;
        simulationState.getProjectileStatusesMutex().unlock();

        simulationState.getAmmos().forEach(Projectile::update);
        simulationState.getCargos().forEach(Projectile::update);

        simulationState.setSimulationTimeS((float) GLFW.glfwGetTime());
    }

    private void updateDronesInAir() {
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
    }

    public void requestFirstDrone() {
        var drone = requestNewDrone();
        simulationState.setCurrentlyControlledDrone(drone.orElseThrow(() -> new RuntimeException("Failed to request first drone")));
    }

    public void respawnDrone() {
        DroneCommunication oldDrone = null;
        if(simulationState.getCurrentlyControlledDrone().isPresent())
            oldDrone = simulationState.getCurrentlyControlledDrone().get();
        var newDrone = requestNewDrone();
        if(newDrone.isEmpty()) {
            notifySubscriber(new Message("Server denied request for a new UAV", "droneRequest", 10, Color.ORANGE, false));
            return;
        }
        simulationState.setCurrentlyControlledDrone(newDrone.get());
        if(oldDrone != null)
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

    @Override
    public void close() {
        droneStatusConsumer.stop();
        projectileStatusesConsumer.stop();
        notificationsConsumer.interrupt();
//        heartbeatProducer.stop();
    }

    @Override
    public List<Consumer<Message>> getSubscribers() {
        return subscribers;
    }
}
