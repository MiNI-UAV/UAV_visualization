package org.uav.scene.gui.widget;

import org.uav.config.Config;
import org.uav.scene.GuiWidget;
import org.uav.scene.gui.GuiAnchorPoint;
import org.uav.scene.gui.GuiElement;
import org.uav.scene.shader.Shader;

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
