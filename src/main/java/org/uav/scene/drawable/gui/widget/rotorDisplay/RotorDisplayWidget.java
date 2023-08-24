package org.uav.scene.drawable.gui.widget.rotorDisplay;

import org.joml.Vector2i;
import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.GuiWidget;
import org.uav.scene.drawable.gui.GuiAnchorPoint;
import org.uav.scene.drawable.gui.GuiElement;
import org.uav.scene.drawable.gui.widget.rotorDisplay.layers.RotorDisplayLayer;
import org.uav.scene.shader.Shader;

public class RotorDisplayWidget implements GuiWidget {
    private final GuiElement guiElement;
    private final RotorDisplayLayer rotorDisplay;
    private final SimulationState simulationState;
    private final Vector2i canvasSize;


    public RotorDisplayWidget(SimulationState simulationState, Config config) {
        this.simulationState = simulationState;

        canvasSize = new Vector2i(400, 400);
        rotorDisplay = new RotorDisplayLayer(canvasSize);

        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(-0.4f, -1.0f, 0.4f, 1.0f)
                .setAnchorPoint(GuiAnchorPoint.BOTTOM_RIGHT)
                .setScale(config.guiScale)
                .setResolution(config.windowWidth, config.windowHeight)
                .setHidden(false)
                .setOverlayLevel(2)
                .addLayer(canvasSize.x, canvasSize.y, rotorDisplay)
                .build();
    }

    public void update() { //  TODO Get rid of all stupid interfaces or rethink them.
        if(guiElement.getHidden()) return;
        rotorDisplay.update(simulationState);
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }
}
