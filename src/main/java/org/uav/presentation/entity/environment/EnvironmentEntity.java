package org.uav.presentation.entity.environment;

import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.model.Model;
import org.uav.presentation.rendering.OrderedRenderQueue;
import org.uav.presentation.rendering.Shader;

public class EnvironmentEntity {
    private final Model environmentModel;

    public EnvironmentEntity(Model environmentModel) {
        this.environmentModel = environmentModel;
    }

    public void draw(SimulationState simulationState, Shader shader) {
        var renderQueue = new OrderedRenderQueue(simulationState.getCamera().getCameraPos());
        environmentModel.addToQueue(renderQueue, shader);
        renderQueue.render();
    }
}
