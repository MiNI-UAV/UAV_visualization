package org.uav.presentation.entity.gui.widget;

import org.joml.Vector4f;
import org.uav.logic.config.Config;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;

public class ControlPanelWidget extends Widget {

    private final Sprite controlPanelSprite;

    public ControlPanelWidget(BufferedImage background, Shader spriteShader, Config config) {
        super(getWidgetPosition(), GuiAnchorPoint.BOTTOM_FULL, config);
        controlPanelSprite = new Sprite(background, spriteShader);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(-0.4f, -1.0f, -1.0f, 1.0f);
    }

    @Override
    protected void drawWidget() {
        controlPanelSprite.draw();
    }
}
