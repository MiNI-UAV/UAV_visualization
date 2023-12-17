package org.uav.presentation.entity.gui.widget;

import org.uav.logic.config.Config;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.GuiElement;
import org.uav.presentation.entity.gui.GuiWidget;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;

public class ControlPanelWidget implements GuiWidget {

    private final GuiElement guiElement;

    public ControlPanelWidget(BufferedImage background, Config config) {
        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(-0.4f, -1.0f, -1.0f, 1.0f)
                .setAnchorPoint(GuiAnchorPoint.BOTTOM_FULL)
                .setScale(config.getGraphicsSettings().getGuiScale())
                .setResolution(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight())
                .setHidden(false)
                .addLayer(background)
                .build();
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }
}
