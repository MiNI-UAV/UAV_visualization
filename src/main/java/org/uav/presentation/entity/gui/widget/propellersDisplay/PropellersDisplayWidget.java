package org.uav.presentation.entity.gui.widget.propellersDisplay;

import org.joml.Vector2i;
import org.joml.Vector4f;
import org.uav.logic.config.Config;
import org.uav.logic.config.DroneParameters;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.rendering.Shader;

public class PropellersDisplayWidget extends Widget {
    private final PropellersDisplayLayer propellersDisplay;
    private final SimulationState simulationState;


    public PropellersDisplayWidget(SimulationState simulationState, DroneParameters droneParameters, Shader vectorShader, Shader circleArcShader, Shader textShader, Config config) {
        super(getWidgetPosition(), GuiAnchorPoint.BOTTOM_RIGHT, config);
        this.simulationState = simulationState;

        Vector2i canvasSize = new Vector2i(400, 400);
        propellersDisplay = new PropellersDisplayLayer(canvasSize, getScaledPosition(), droneParameters, vectorShader, circleArcShader, textShader, config);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(-0.4f, -1.0f, 0.4f, 1.0f);
    }


    public void update() {
        propellersDisplay.update(simulationState);
    }

    @Override
    protected void drawWidget() {
        propellersDisplay.draw();
    }
}
