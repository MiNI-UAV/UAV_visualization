package org.uav.scene.drawable.gui.widget.map;

import org.joml.Vector2i;
import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.GuiWidget;
import org.uav.scene.drawable.gui.GuiAnchorPoint;
import org.uav.scene.drawable.gui.GuiElement;
import org.uav.scene.drawable.gui.widget.map.layers.MapProjectionLayer;
import org.uav.scene.shader.Shader;

import java.awt.image.BufferedImage;

public class MapWidget implements GuiWidget {
    private final GuiElement guiElement;
    private final MapProjectionLayer mapProjection;
    private final SimulationState simulationState;


    public MapWidget(BufferedImage background, BufferedImage mapImage, BufferedImage droneImage, SimulationState simulationState, Config config) {
        this.simulationState = simulationState;
        Vector2i mapResolution = new Vector2i(1000, 1000);
        float mapScale = 1000f / 240;
        mapProjection = new MapProjectionLayer(mapImage, droneImage, mapScale, mapResolution);

        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(0.8f, -0.8f, -0.8f, 0.8f)
                .setAnchorPoint(GuiAnchorPoint.CENTER)
                .setScale(config.guiScale)
                .setResolution(config.windowWidth, config.windowHeight)
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