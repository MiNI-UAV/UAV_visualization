package org.uav.presentation.entity.gui.widget.propellersDisplay;

import org.joml.Vector2i;
import org.uav.logic.config.Config;
import org.uav.logic.config.DroneParameters;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.GuiElement;
import org.uav.presentation.entity.gui.GuiWidget;
import org.uav.presentation.rendering.Shader;

public class PropellersDisplayWidget implements GuiWidget {
    private final GuiElement guiElement;
    private final PropellersDisplayLayer propellersDisplay;
    private final SimulationState simulationState;


    public PropellersDisplayWidget(SimulationState simulationState, Config config, DroneParameters droneParameters) {
        this.simulationState = simulationState;

        Vector2i canvasSize = new Vector2i(400, 400);
        propellersDisplay = new PropellersDisplayLayer(canvasSize, droneParameters);

        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(-0.4f, -1.0f, 0.4f, 1.0f)
                .setAnchorPoint(GuiAnchorPoint.BOTTOM_RIGHT)
                .setScale(config.getGraphicsSettings().getGuiScale())
                .setResolution(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight())
                .setHidden(false)
                .setOverlayLevel(2)
                .addLayer(canvasSize.x, canvasSize.y, propellersDisplay)
                .build();
    }

    public void update() {
        if(guiElement.getHidden()) return;
        propellersDisplay.update(simulationState);
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }
}
