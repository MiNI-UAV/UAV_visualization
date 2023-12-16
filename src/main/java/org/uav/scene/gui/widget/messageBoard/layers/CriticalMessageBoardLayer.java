package org.uav.scene.gui.widget.messageBoard.layers;

import org.javatuples.Pair;
import org.uav.messages.Message;
import org.uav.messages.MessageBoard;
import org.uav.scene.gui.DrawableGuiLayer;

import java.awt.*;

public class CriticalMessageBoardLayer implements DrawableGuiLayer {
    private static final int FONT_SIZE = 50;
    private static final float FADING_TIME_LEFT_S = 0.5f;
    private final MessageBoard messageBoard;
    private final int imageWidth;

    public CriticalMessageBoardLayer(MessageBoard messageBoard, int imageWidth) {
        this.messageBoard = messageBoard;
        this.imageWidth = imageWidth;
    }


    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("Roboto", Font.PLAIN, FONT_SIZE));

        int fontHeight = g.getFontMetrics().getHeight();
        for(int i=0; i<messageBoard.getCriticalMessages().size(); i++) {
            Pair<Float, Message> message = messageBoard.getCriticalMessages().get(i);
            String content = message.getValue1().getContent();
            int contentWidth = (int) g.getFontMetrics().getStringBounds(content, g).getWidth();
            g.setColor(getMessageColor(message.getValue1().getColor(), messageBoard.getTimeLeft(message)));
            g.drawString(content, (imageWidth-contentWidth)/2, fontHeight * (i+1));
        }
    }

    public Color getMessageColor(Color color, float timeLeft) {
        if(timeLeft > FADING_TIME_LEFT_S) return color;
        if(timeLeft < 0) return new Color(0, true);
        return new Color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, timeLeft / FADING_TIME_LEFT_S);
    }
}
