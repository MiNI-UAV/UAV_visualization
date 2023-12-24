package org.uav.presentation.entity.gui.widget.map;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.uav.logic.config.Config;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;

public class MapWidget extends Widget {
    private final MapProjectionLayer mapProjection;
    private final SimulationState simulationState;
    private final Sprite backgroundSprite;



    public MapWidget(BufferedImage background, BufferedImage mapImage, BufferedImage droneImage, BufferedImage droneImageDemanded, SimulationState simulationState, Shader spriteShader, Shader vectorShader, Config config) {
        super(getWidgetPosition(), GuiAnchorPoint.CENTER, config, true);
        this.simulationState = simulationState;
        Vector2i mapResolution = new Vector2i(1000, 1000);
        Vector2f mapScale =  new Vector2f(2.f, 2.f); // Map scale 2pixel:1meter
        mapProjection = new MapProjectionLayer(mapImage, droneImage, droneImageDemanded, mapScale, mapResolution, spriteShader, vectorShader);
        backgroundSprite = new Sprite(background, spriteShader);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(0.8f, -0.8f, -0.8f, 0.8f);
    }


    public void update() {
        mapProjection.update(simulationState);
    }

    @Override
    protected void drawWidget() {
        backgroundSprite.draw();
        mapProjection.draw();
    }
}
