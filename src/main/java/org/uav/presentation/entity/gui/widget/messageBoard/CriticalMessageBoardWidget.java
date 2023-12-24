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

public class CriticalMessageBoardWidget extends Widget {
    private static final float FONT_SIZE_NORM = 100f / 1080;
    private static final float FADING_TIME_LEFT_S = 0.5f;
    private MessageBoard messageBoard;
    private TextEngine textEngine;

    public CriticalMessageBoardWidget(Shader textShader, Config config, MessageBoard messageBoard) {
        super(getWidgetPosition(), GuiAnchorPoint.CENTER, config);
        this.messageBoard = messageBoard;
        textEngine = new TextEngine(getScaledPosition(), FONT_SIZE_NORM, textShader, config);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(1f, -1f, -1f, 1f);
    }

    @Override
    protected void drawWidget() {
        for(int i=0; i<messageBoard.getCriticalMessages().size(); i++) {
            Pair<Float, Message> message = messageBoard.getCriticalMessages().get(i);
            var color = getMessageColor(message.getValue1().getColor(), messageBoard.getTimeLeft(message));
            var content = message.getValue1().getContent();
            textEngine.setColor(new Vector4f(color.getComponents(new float[4])));
            textEngine.setPosition(-textEngine.getStringWidth(content)/2, -i * textEngine.getFontHeight());
            textEngine.renderText(message.getValue1().getContent());
        }
    }

    public Color getMessageColor(Color color, float timeLeft) {
        if(timeLeft > FADING_TIME_LEFT_S) return color;
        if(timeLeft < 0) return new Color(0, true);
        return new Color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, timeLeft / FADING_TIME_LEFT_S);
    }
}
