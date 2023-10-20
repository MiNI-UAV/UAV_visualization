package org.uav.scene.gui.widget.display;

import org.uav.config.Config;
import org.uav.scene.GuiWidget;
import org.uav.scene.gui.GuiAnchorPoint;
import org.uav.scene.gui.GuiElement;
import org.uav.scene.shader.Shader;

import java.util.List;

public class DisplayWidget implements GuiWidget {
    private final GuiElement guiElement;
    private final DisplayLayer displayLayer;

    public DisplayWidget(Config config, List<String> description) {
        displayLayer = new DisplayLayer(description);
        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(1f, -1f, -1f, 1f)
                .setAnchorPoint(GuiAnchorPoint.CENTER)
                .setScale(config.getGraphicsSettings().getGuiScale())
                .setResolution(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight())
                .setHidden(false)
                .addLayer(1000, 1000, displayLayer)
                .build();
    }


    public void update(List<String> description) {
        if(guiElement.getHidden()) return;
        displayLayer.update(description);
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }
}
