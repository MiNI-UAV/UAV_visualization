package org.uav.presentation.entity.gui.widget.display;

import org.joml.Vector4f;
import org.uav.logic.config.Config;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.entity.text.TextEngine;
import org.uav.presentation.rendering.Shader;

import java.util.List;

public class DisplayWidget extends Widget {
    private static final float FONT_SIZE_NORM = 50f / 1080;
    private final TextEngine textEngine;
    public String text;

    public DisplayWidget(Shader textShader, Config config) {
        super(getWidgetPosition(), GuiAnchorPoint.NONE, config);
        text = "";
        textEngine = new TextEngine(getScaledPosition(), FONT_SIZE_NORM, textShader, config);
        textEngine.setPosition(-0.8f, 0.8f);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(1f, -1f, -1f, 1f);
    }

    public void update(List<String> text) {
        this.text = text.stream().reduce("", (String result, String app) -> result + "\n" + app);
    }

    public void update(String text) {
        this.text = text;
    }

    @Override
    protected void drawWidget() {
        textEngine.renderText(text);
    }
}
