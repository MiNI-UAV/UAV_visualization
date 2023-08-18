package org.uav.scene.drawable.gui.widget.artificialHorizon;

import org.uav.config.Config;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.GuiWidget;
import org.uav.scene.drawable.gui.GuiAnchorPoint;
import org.uav.scene.drawable.gui.GuiElement;
import org.uav.scene.drawable.gui.widget.artificialHorizon.layers.ArtificialHorizonBackgroundLayer;
import org.uav.scene.drawable.gui.widget.artificialHorizon.layers.ArtificialHorizonCompassLayer;
import org.uav.scene.drawable.gui.widget.artificialHorizon.layers.ArtificialHorizonMetersLayer;
import org.uav.scene.drawable.gui.widget.artificialHorizon.layers.ArtificialHorizonRollLayer;
import org.uav.scene.shader.Shader;

import java.awt.image.BufferedImage;

public class ArtificialHorizonWidget implements GuiWidget {
    private final GuiElement guiElement;
    private final ArtificialHorizonBackgroundLayer backgroundLayer;
    private final ArtificialHorizonCompassLayer compassLayer;
    private final ArtificialHorizonRollLayer rollLayer;
    private final ArtificialHorizonMetersLayer metersLayer;
    private final SimulationState simulationState;

    public ArtificialHorizonWidget(
            BufferedImage horizonTexture,
            BufferedImage horizonCursorTexture,
            BufferedImage horizonRollTexture,
            BufferedImage compassTexture,
            SimulationState simulationState,
            Config config
    ) {
        this.simulationState = simulationState;
        int distanceToMax = 360;
        int horizonScreenX = 480;
        int horizonScreenY = 360;

        backgroundLayer = new ArtificialHorizonBackgroundLayer(horizonTexture, distanceToMax, horizonScreenX, horizonScreenY);
        compassLayer = new ArtificialHorizonCompassLayer(compassTexture, 30, horizonScreenX);
        rollLayer = new ArtificialHorizonRollLayer(horizonRollTexture, horizonScreenX, horizonScreenY);
        metersLayer = new ArtificialHorizonMetersLayer(horizonScreenX, horizonScreenY);

        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(-0.4f, -1.0f, -0.4f, 0.4f)
                .setAnchorPoint(GuiAnchorPoint.BOTTOM)
                .setScale(config.guiScale)
                .setResolution(config.windowWidth, config.windowHeight)
                .setHidden(false)
                .addLayer(horizonScreenX, horizonScreenY, backgroundLayer)
                .addLayer(horizonCursorTexture)
                .addLayer(horizonScreenX, horizonScreenY, rollLayer)
                .addLayer(horizonScreenX, horizonScreenY, compassLayer)
                .addLayer(horizonScreenX, horizonScreenY, metersLayer)
                .build();
    }

    public void update() {
        if(guiElement.getHidden()) return;
        backgroundLayer.update(simulationState);
        compassLayer.update(simulationState);
        rollLayer.update(simulationState);
        metersLayer.update(simulationState);
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }

}
