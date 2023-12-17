package org.uav.presentation.entity.abstraction;

import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.state.controlMode.ControlModeDemanded;
import org.uav.logic.state.controlMode.ControlModeReply;
import org.uav.presentation.model.Model;
import org.uav.presentation.rendering.Shader;

import java.util.List;

import static org.uav.logic.state.controlMode.ControlModeReply.*;
import static org.uav.utils.Convert.toQuaternion;

@AllArgsConstructor
public class XMarkEntity {

    public static final List<ControlModeReply> xMarkDemands = List.of(X, Y, Z, YAW);

    private final Model xMarkModel;

    public void draw(ControlModeDemanded currentControlModeDemanded, MemoryStack stack, Shader shader) {
        if(currentControlModeDemanded == null) return;
        var demanded = currentControlModeDemanded.demanded;
        if(xMarkDemands.stream().allMatch(demanded::containsKey)) {
            float demandedX = demanded.get(ControlModeReply.X);
            float demandedY = demanded.get(Y);
            float demandedZ = demanded.get(Z);
            float demandedYaw = demanded.get(YAW);
            xMarkModel.setPosition(new Vector3f(demandedX, demandedY, demandedZ));
            xMarkModel.setRotation(toQuaternion(new Vector3f(0, 0, demandedYaw)));
            xMarkModel.draw(stack, shader);
        }
    }
}
