package org.uav.logic.messages;

import lombok.Value;

import java.awt.*;

@Value
public class Message {
    String content;
    String category;
    float showTimeS;
    Color color;
    boolean isCritical;

    public Message(String content, String category, float showTimeS, Color color, boolean isCritical) {
        this.content = content;
        this.category = category;
        this.showTimeS = showTimeS;
        this.color = color;
        this.isCritical = isCritical;
    }

    public Message(String content, String category, int showTimeS) {
        this(content, category, showTimeS, Color.WHITE, false);
    }
}
