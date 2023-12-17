package org.uav.presentation.entity.gui.widget.display;

import org.uav.presentation.entity.gui.DrawableGuiLayer;

import java.awt.*;
import java.util.List;

public class DisplayLayer implements DrawableGuiLayer {
    private static final int FONT_SIZE = 20;
    public List<String> description;

    public DisplayLayer(List<String> description) {
        this.description = description;
    }

    public void update(List<String> description) {
        this.description = description;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
        g.setColor(Color.white);

        //String modeString = MessageFormat.format("{0}", description);
        int fontHeight = g.getFontMetrics().getHeight();
        for(int i=0; i<description.size(); i++)
            g.drawString(description.get(i), 100, 100 + fontHeight * i);
    }
}
