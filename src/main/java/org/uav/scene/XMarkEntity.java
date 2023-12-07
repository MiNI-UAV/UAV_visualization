package org.uav.scene;

import lombok.AllArgsConstructor;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.model.Model;
import org.uav.model.controlMode.ControlModeDemanded;
import org.uav.model.controlMode.ControlModeReply;
import org.uav.scene.shader.Shader;

import static org.uav.utils.Convert.toQuaternion;

@AllArgsConstructor
public class XMarkEntity {
    private final Model xMarkModel;

    public void draw(ControlModeDemanded currentControlModeDemanded, MemoryStack stack, Shader shader, float time) {
        if(currentControlModeDemanded == null) return;
        var demanded = currentControlModeDemanded.demanded;
        if(ControlModeReply.xMarkDemands.stream().allMatch(demanded::containsKey)) {
            float demandedX = demanded.get(ControlModeReply.X);
            float demandedY = demanded.get(ControlModeReply.Y);
            float demandedZ = demanded.get(ControlModeReply.Z);
            float demandedYaw = demanded.get(ControlModeReply.YAW);
            xMarkModel.setPosition(new Vector3f(demandedX, demandedY, demandedZ));
            xMarkModel.setRotation(toQuaternion(new Vector3f(0, 0, demandedYaw)));
            xMarkModel.draw(stack, shader, time);
        }
    }
}
