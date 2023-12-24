package org.uav.presentation.entity.gui.widget.messageBoard;

import org.javatuples.Pair;
import org.joml.Vector4f;
import org.uav.logic.config.Config;
import org.uav.logic.messages.Message;
import org.uav.logic.messages.MessageBoard;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.entity.text.TextEngine;
import org.uav.presentation.rendering.Shader;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MessageBoardWidget extends Widget {
    private static final float FONT_SIZE_NORM = 30f / 1080;
    private static final float FADING_TIME_LEFT_S = 0.5f;
    MessageBoard messageBoard;
    private TextEngine textEngine;

    public MessageBoardWidget(Shader textShader, Config config, MessageBoard messageBoard) {
        super(getWidgetPosition(), GuiAnchorPoint.BOTTOM_LEFT, config);
        this.messageBoard = messageBoard;
        textEngine = new TextEngine(getScaledPosition(), FONT_SIZE_NORM, textShader, config);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(1f, -0.4f, -1f, 0.6f);
    }

    @Override
    protected void drawWidget() {
        for(int i=0; i<messageBoard.getMessages().size(); i++) {
            Pair<Float, Message> message = messageBoard.getMessages().get(i);
            var color = getMessageColor(message.getValue1().getColor(), messageBoard.getTimeLeft(message));
            var ds = DecimalFormatSymbols.getInstance();
            ds.setDecimalSeparator('.');
            var content = "[" + new DecimalFormat("#.0", ds).format(message.getValue0()) + "] " + message.getValue1().getContent();
            textEngine.setColor(new Vector4f(color.getComponents(new float[4])));
            textEngine.setPosition(-0.975f, -0.99f + i * textEngine.getFontHeight());
            textEngine.renderText(content);
        }
    }

    public Color getMessageColor(Color color, float timeLeft) {
        if(timeLeft > FADING_TIME_LEFT_S) return color;
        if(timeLeft < 0) return new Color(0, true);
        return new Color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, timeLeft / FADING_TIME_LEFT_S);
    }
}
