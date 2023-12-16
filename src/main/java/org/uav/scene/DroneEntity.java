package org.uav.scene;

import lombok.AllArgsConstructor;
import org.lwjgl.system.MemoryStack;
import org.uav.model.Model;
import org.uav.model.status.DroneState;
import org.uav.model.status.JoystickStatus;
import org.uav.scene.shader.Shader;

import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
public class DroneEntity {
    private final static String DEFAULT_DRONE_MODEL = "defaultDrone";
    private final Map<String, Model> droneModels;

    public void draw(MemoryStack stack, Shader shader, float deltaTimeS, Collection<DroneState> droneStates, JoystickStatus joystickStatus) {
        for(DroneState state: droneStates) {
            draw(stack, shader, deltaTimeS, state, joystickStatus);
        }
    }

    public void draw(MemoryStack stack, Shader shader, float deltaTimeS, DroneState state, JoystickStatus joystickStatus) {
        Model droneModel = droneModels.getOrDefault(state.modelName, droneModels.get(DEFAULT_DRONE_MODEL));
        droneModel.setPosition(state.droneStatus.position);
        droneModel.setRotation(state.droneStatus.rotation);
        state.updateAnimation(droneModel, joystickStatus,deltaTimeS);
        droneModel.draw(stack, shader);
    }
}
