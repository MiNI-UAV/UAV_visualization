package org.uav.presentation.entity.gui.widget.debug;

import org.joml.Vector4f;
import org.uav.logic.config.Config;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.entity.text.TextEngine;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class DebugWidget extends Widget {
    private static final float FONT_SIZE_NORM = 40f / 1080;
    private final SimulationState simulationState;
    private final Sprite backgroundSprite;
    private final TextEngine textEngine;
    private String text;

    public DebugWidget(BufferedImage background, SimulationState simulationState, Shader spriteShader, Shader textShader,  Config config) {
        super(getWidgetPosition(), GuiAnchorPoint.TOP_LEFT, config);
        this.simulationState = simulationState;
        backgroundSprite = new Sprite(background, spriteShader);
        textEngine = new TextEngine(getScaledPosition(), FONT_SIZE_NORM, textShader, config);
        textEngine.setPosition(-0.95f, 0.2f);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(1f, 0.8f, -1f, -0.6f);
    }

    public void update() {
        var fpsCounter = simulationState.getFpsCounter();
        var ds = DecimalFormatSymbols.getInstance();
        ds.setDecimalSeparator('.');
        var df = new DecimalFormat("#.##", ds);
        text =  df.format(fpsCounter.getFramesPerSecond()) + " fps\n";
        text += df.format(fpsCounter.getMillisecondsPerFrame()) + " mspf";
    }

    @Override
    protected void drawWidget() {
        backgroundSprite.draw();
        textEngine.renderText(text);
    }
}
