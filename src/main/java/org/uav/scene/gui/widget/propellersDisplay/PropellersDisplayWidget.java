package org.uav.scene.drawable.gui.widget.propellersDisplay;

import org.joml.Vector2i;
import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.model.SimulationState;
import org.uav.scene.GuiWidget;
import org.uav.scene.gui.GuiAnchorPoint;
import org.uav.scene.gui.GuiElement;
import org.uav.scene.shader.Shader;

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

    public void update() { //  TODO Get rid of all stupid interfaces or rethink them.
        if(guiElement.getHidden()) return;
        propellersDisplay.update(simulationState);
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }
}
