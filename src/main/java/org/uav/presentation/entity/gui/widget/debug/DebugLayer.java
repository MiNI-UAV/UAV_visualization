package org.uav.presentation.entity.gui.widget.debug;

import org.uav.logic.fps.FpsCounter;
import org.uav.presentation.entity.gui.DrawableGuiLayer;
import org.uav.utils.SignificantDigitsRounder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DebugLayer implements DrawableGuiLayer {
    private static final int FONT_SIZE = 30;
    private final List<String> description;

    public DebugLayer() {
        description = new ArrayList<>();
    }

    public void update(FpsCounter fpsCounter) {
        description.clear();
        description.add(SignificantDigitsRounder.sigDigRounder(fpsCounter.getFramesPerSecond(), 4 , 0) + " fps");
        description.add(SignificantDigitsRounder.sigDigRounder(fpsCounter.getMillisecondsPerFrame(), 4 , 0) + " ms/f");
    }

    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
        g.setColor(Color.white);

        int fontHeight = g.getFontMetrics().getHeight();
        for(int i=0; i<description.size(); i++)
            g.drawString(description.get(i), 0, 100 + fontHeight * i);
    }
}
