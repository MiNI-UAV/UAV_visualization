package org.uav.scene.gui.widget.bindings;

import org.uav.scene.gui.DrawableGuiLayer;

import java.awt.*;

public class LoadingDescriptionLayer implements DrawableGuiLayer {
    private static final int FONT_SIZE = 20;
    public String description;

    public LoadingDescriptionLayer(String description) {
        this.description = description;
    }

    public void update(String description) {
        this.description = description;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
        g.setColor(Color.white);

        //String modeString = MessageFormat.format("{0}", description);
        g.drawString(description, 100, 100);
    }
}
