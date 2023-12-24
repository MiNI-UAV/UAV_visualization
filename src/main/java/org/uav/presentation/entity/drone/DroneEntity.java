package org.uav.presentation.entity.drone;

import lombok.AllArgsConstructor;
import org.uav.logic.input.handler.JoystickStatus;
import org.uav.presentation.model.Model;
import org.uav.presentation.rendering.Shader;

import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
public class DroneEntity {
    private final static String DEFAULT_DRONE_MODEL = "defaultDrone";
    private final Map<String, Model> droneModels;

    public void draw(Shader shader, float deltaTimeS, Collection<DroneState> droneStates, JoystickStatus joystickStatus) {
        for(DroneState state: droneStates) {
            draw(shader, deltaTimeS, state, joystickStatus);
        }
    }

    public void draw(Shader shader, float deltaTimeS, DroneState state, JoystickStatus joystickStatus) {
        Model droneModel = droneModels.getOrDefault(state.modelName, droneModels.get(DEFAULT_DRONE_MODEL));
        droneModel.setPosition(state.droneStatus.position);
        droneModel.setRotation(state.droneStatus.rotation);
        state.updateAnimation(droneModel, joystickStatus,deltaTimeS);
        droneModel.draw(shader);
    }
}
