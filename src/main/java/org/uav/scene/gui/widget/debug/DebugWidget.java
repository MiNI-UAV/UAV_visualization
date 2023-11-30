package org.uav.scene.gui.widget.debug;

import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.scene.GuiWidget;
import org.uav.scene.gui.GuiAnchorPoint;
import org.uav.scene.gui.GuiElement;
import org.uav.scene.shader.Shader;

import java.awt.image.BufferedImage;

public class DebugWidget implements GuiWidget {
    private final GuiElement guiElement;
    private final DebugLayer debugLayer;
    private final SimulationState simulationState;

    public DebugWidget(BufferedImage background, SimulationState simulationState, Config config) {
        this.simulationState = simulationState;
        debugLayer = new DebugLayer();
        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(1f, 0.8f, -1f, -0.6f)
                .setAnchorPoint(GuiAnchorPoint.TOP_LEFT)
                .setScale(config.getGraphicsSettings().getGuiScale())
                .setResolution(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight())
                .setHidden(!config.getGraphicsSettings().isShowDebugInfo())
                .addLayer(background)
                .addLayer(400, 200, debugLayer)
                .build();
    }


    public void update() {
        if(guiElement.getHidden()) return;
        debugLayer.update(simulationState.getFpsCounter());
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }
}
