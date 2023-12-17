package org.uav.presentation.entity.environment;

import org.lwjgl.system.MemoryStack;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.model.Model;
import org.uav.presentation.rendering.OrderedRenderQueue;
import org.uav.presentation.rendering.Shader;

public class EnvironmentEntity {
    private final Model environmentModel;

    public EnvironmentEntity(Model environmentModel) {
        this.environmentModel = environmentModel;
    }

    public void draw(SimulationState simulationState, MemoryStack stack, Shader shader) {
        var renderQueue = new OrderedRenderQueue(simulationState.getCamera().getCameraPos());
        environmentModel.addToQueue(renderQueue, shader);
        renderQueue.render(stack);
    }
}
