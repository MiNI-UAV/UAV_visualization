package org.uav.presentation.entity.gui.widget.map;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.uav.logic.config.Config;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.GuiElement;
import org.uav.presentation.entity.gui.GuiWidget;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;

public class MapWidget implements GuiWidget {
    private final GuiElement guiElement;
    private final MapProjectionLayer mapProjection;
    private final SimulationState simulationState;


    public MapWidget(BufferedImage background, BufferedImage mapImage, BufferedImage droneImage, BufferedImage droneImageDemanded, SimulationState simulationState, Config config) {
        this.simulationState = simulationState;
        Vector2i mapResolution = new Vector2i(1000, 1000);
        Vector2f mapScale =  new Vector2f(2.f, 2.f); // Map scale 2pixel:1meter
        mapProjection = new MapProjectionLayer(mapImage, droneImage, droneImageDemanded, mapScale, mapResolution);

        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(0.8f, -0.8f, -0.8f, 0.8f)
                .setAnchorPoint(GuiAnchorPoint.CENTER)
                .setScale(config.getGraphicsSettings().getGuiScale())
                .setResolution(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight())
                .setHidden(true)
                .setOverlayLevel(9)
                .addLayer(background)
                .addLayer(mapResolution.x, mapResolution.y, mapProjection)
                .build();
    }

    public void setHidden(boolean hidden) {
        guiElement.setHidden(hidden);
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }

    public void update() {
        if(guiElement.getHidden()) return;
        mapProjection.update(simulationState);
    }

}