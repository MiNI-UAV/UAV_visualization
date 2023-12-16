package org.uav.model.status;

import org.uav.model.Model;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class DroneState {
    private static final float VISUALIZATION_MODIFIER = 0.2f;
    public static final String ANIMATION_PROPELLER = "propeller";
    public static final String ANIMATION_RUDDER = "rudder";
    public static final String ANIMATION_ELEVATOR = "elevator";
    public static final String ANIMATION_AILERON = "aileron";
    public DroneStatus droneStatus;
    @Nullable
    public String modelName;

    public DroneState(DroneStatus droneStatus) {
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

    }

    private void updateFlightControlSurfaceAnimation(List<Model.AnimationInfo> animations, JoystickStatus joystickStatus, int axisId, String identificator) {
        if(joystickStatus == null) return;
        animations.stream()
                .filter(info -> info.getAnimatedModelName().contains(identificator))
                .forEach(controlSurface -> {
            if(joystickStatus.axes.size() > axisId)
                controlSurface.setAnimationProgress((joystickStatus.axes.get(axisId) + 1) / 2);
        });
    }

    private void updatePropellersAnimation(List<Model.AnimationInfo> animations, float deltaTimeS) {
        var propellerAnimations = animations.stream()
                .filter(info -> info.getAnimatedModelName().contains(ANIMATION_PROPELLER))
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
