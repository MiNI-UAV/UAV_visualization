package org.uav.scene.drawable.gui.widget.loading;

import org.uav.config.Config;
import org.uav.scene.drawable.GuiWidget;
import org.uav.scene.drawable.gui.GuiAnchorPoint;
import org.uav.scene.drawable.gui.GuiElement;
import org.uav.scene.drawable.gui.widget.loading.layers.LoadingDescriptionLayer;
import org.uav.scene.shader.Shader;

public class LoadingScreenWidget implements GuiWidget {
    private final GuiElement guiElement;
    private final LoadingDescriptionLayer loadingDescription;

    public LoadingScreenWidget(Config config, String description) {
        loadingDescription = new LoadingDescriptionLayer(description);
        guiElement = new GuiElement.GuiElementBuilder()
                .setPosition(1f, -1f, -1f, 1f)
                .setAnchorPoint(GuiAnchorPoint.BOTTOM_RIGHT)
                .setScale(config.guiScale)
                .setResolution(config.windowWidth, config.windowHeight)
                .setHidden(false)
                .addLayer(1000, 1000, loadingDescription)
                .build();
    }


    public void update(String description) {
        if(guiElement.getHidden()) return;
        loadingDescription.update(description);
    }

    @Override
    public void draw(Shader shader) {
        guiElement.draw(shader);
    }
}
