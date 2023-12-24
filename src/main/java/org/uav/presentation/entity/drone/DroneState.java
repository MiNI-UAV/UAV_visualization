package org.uav.presentation.entity.drone;

import org.uav.logic.input.handler.JoystickStatus;
import org.uav.logic.state.drone.DroneStatus;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.model.Model;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class DroneState {
    private static final float VISUALIZATION_MODIFIER = 0.2f;
    private static final String ANIMATION_PROPELLER = "propeller";
    private static final String ANIMATION_RUDDER = "rudder";
    private static final String ANIMATION_ELEVATOR = "elevator";
    private static final String ANIMATION_AILERON = "aileron";
    private static final String ANIMATION_MISSILE = "missile";
    private static final String ANIMATION_LEFT = "left";
    private static final String ANIMATION_RIGHT = "right";
    private final SimulationState simulationState;

    public DroneStatus droneStatus;
    @Nullable
    public String modelName;

    public DroneState(SimulationState simulationState, DroneStatus droneStatus) {
        this.simulationState = simulationState;
        this.droneStatus = droneStatus;
        modelName = null;
    }

    public DroneState update(DroneStatus droneStatus, Map<Integer, String> droneModels) {
        this.droneStatus = droneStatus;
        modelName = droneModels.get(droneStatus.id);
        return this;
    }

    public void updateAnimation(Model model, JoystickStatus joystickStatus, float deltaTimeS) {
        var animations = model.getAnimationInfos();
        updatePropellersAnimation(animations, deltaTimeS);
        updateFlightControlSurfaceAnimation(animations, joystickStatus, 1, ANIMATION_AILERON);
        updateFlightControlSurfaceAnimation(animations, joystickStatus, 2, ANIMATION_ELEVATOR);
        updateFlightControlSurfaceAnimation(animations, joystickStatus, 3, ANIMATION_RUDDER);
        updateMissile(animations, ANIMATION_LEFT);
        updateMissile(animations, ANIMATION_RIGHT);
    }

    private void updateMissile(List<Model.AnimationInfo> animations, String identificator) {
        var launched = simulationState.getAmmos().stream()
                .anyMatch(a -> a.name.toLowerCase().contains(identificator) && a.name.toLowerCase().contains(ANIMATION_MISSILE) && a.currentAmount == 0);
        var missiles = animations.stream()
                .filter(info -> info.getAnimatedModelName().toLowerCase().contains(identificator) && info.getAnimatedModelName().toLowerCase().contains(ANIMATION_MISSILE));
        if(launched) missiles.forEach(missile -> missile.setAnimationProgress(1));
        else missiles.forEach(missile -> missile.setAnimationProgress(0));
    }

    private void updateFlightControlSurfaceAnimation(List<Model.AnimationInfo> animations, JoystickStatus joystickStatus, int axisId, String identificator) {
        if(joystickStatus == null) return;
        animations.stream()
                .filter(info -> info.getAnimatedModelName().toLowerCase().contains(identificator))
                .forEach(controlSurface -> {
            if(joystickStatus.axes.size() > axisId)
                controlSurface.setAnimationProgress((joystickStatus.axes.get(axisId) + 1) / 2);
        });
    }

    private void updatePropellersAnimation(List<Model.AnimationInfo> animations, float deltaTimeS) {
        var propellerAnimations = animations.stream()
                .filter(info -> info.getAnimatedModelName().toLowerCase().contains(ANIMATION_PROPELLER))
                .sorted(Comparator.comparing(Model.AnimationInfo::getAnimatedModelName))
                .toList();
        var rotations = droneStatus.propellersRadps.stream()
                .map(radps -> radps / (2 * (float) Math.PI) / 3600 * VISUALIZATION_MODIFIER)
                .map(rotps -> rotps * deltaTimeS).toList();
        for(int i=0; i< rotations.size() && i< propellerAnimations.size(); i++) {
            float nextRotation = propellerAnimations.get(i).getAnimationProgress() + rotations.get(i);
            float cutRotation = nextRotation % 1;
            propellerAnimations.get(i).setAnimationProgress(cutRotation);
        }
    }
}
