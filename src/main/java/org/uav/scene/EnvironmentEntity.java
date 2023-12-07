package org.uav.scene;

import org.lwjgl.system.MemoryStack;
import org.uav.model.Model;
import org.uav.model.SimulationState;
import org.uav.scene.shader.Shader;

public class EnvironmentEntity {
    private final Model environmentModel;

    public EnvironmentEntity(Model environmentModel) {
        this.environmentModel = environmentModel;
    }

    public void draw(SimulationState simulationState, MemoryStack stack, Shader shader, float time) {
        var renderQueue = new OrderedRenderQueue(simulationState.getCamera().getCameraPos());
        environmentModel.addToQueue(renderQueue, shader, time);
        renderQueue.render(stack);
    }
}
