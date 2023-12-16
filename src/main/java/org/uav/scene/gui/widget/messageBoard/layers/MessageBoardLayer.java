package org.uav.scene.gui.widget.messageBoard.layers;

import org.javatuples.Pair;
import org.uav.messages.Message;
import org.uav.messages.MessageBoard;
import org.uav.scene.gui.DrawableGuiLayer;

import java.awt.*;
import java.text.DecimalFormat;

public class MessageBoardLayer implements DrawableGuiLayer {
    private static final int FONT_SIZE = 10;
    private static final float FADING_TIME_LEFT_S = 0.5f;
    private final MessageBoard messageBoard;
    private final int imageHeight;

    public MessageBoardLayer(MessageBoard messageBoard, int imageHeight) {
        this.messageBoard = messageBoard;
        this.imageHeight = imageHeight;
    }


    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));

        int fontHeight = g.getFontMetrics().getHeight();
        for(int i=0; i<messageBoard.getMessages().size(); i++) {
            Pair<Float, Message> message = messageBoard.getMessages().get(i);
            g.setColor(getMessageColor(message.getValue1().getColor(), messageBoard.getTimeLeft(message)));
            g.drawString("[" + new DecimalFormat("#.0").format(message.getValue0()) + "] " + message.getValue1().getContent(), 0, imageHeight - fontHeight * (i+1));
        }
    }

    public Color getMessageColor(Color color, float timeLeft) {
        if(timeLeft > FADING_TIME_LEFT_S) return color;
        if(timeLeft < 0) return new Color(0, true);
        return new Color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, timeLeft / FADING_TIME_LEFT_S);
    }
}
