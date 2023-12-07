package org.uav.scene;

import lombok.AllArgsConstructor;
import org.lwjgl.system.MemoryStack;
import org.uav.model.Model;
import org.uav.model.SimulationState;
import org.uav.model.status.DroneStatus;
import org.uav.scene.shader.Shader;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class DroneEntity {
    private final static String DEFAULT_DRONE_MODEL = "defaultDrone";
    private final SimulationState simulationState;
    private final Map<String, Model> droneModels;

    public void draw(MemoryStack stack, Shader shader, float time, Collection<DroneStatus> droneStatuses) {
        for(DroneStatus status: droneStatuses) {
            draw(stack, shader, time, status);
        }
    }

    public void draw(MemoryStack stack, Shader shader, float time, DroneStatus status) {
        String droneModelName = simulationState.getNotifications().droneModelsNames.getOrDefault(status.id, DEFAULT_DRONE_MODEL);
        Model droneModel = droneModels.getOrDefault(droneModelName, droneModels.get(DEFAULT_DRONE_MODEL));
        droneModel.draw(stack, shader, time);
        droneModel.setPosition(status.position);
        droneModel.setRotation(status.rotation);
    }

    public Optional<DroneStatus> getPlayerDrone() {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().getId());
        return Optional.ofNullable(drone);
    }
}
