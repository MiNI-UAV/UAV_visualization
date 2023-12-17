package org.uav.presentation.entity.gui.widget.messageBoard;

import org.uav.logic.config.Config;
import org.uav.logic.messages.MessageBoard;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.GuiElement;
import org.uav.presentation.entity.gui.GuiWidget;
import org.uav.presentation.entity.gui.widget.messageBoard.layers.CriticalMessageBoardLayer;
import org.uav.presentation.entity.gui.widget.messageBoard.layers.MessageBoardLayer;
import org.uav.presentation.rendering.Shader;

public class MessageBoardWidget implements GuiWidget {
    private final GuiElement guiElementMessages;
    private final GuiElement guiElementCritical;
    private final MessageBoardLayer messageBoardLayer;
    private final CriticalMessageBoardLayer criticalMessageBoardLayer;
    MessageBoard messageBoard;

    public MessageBoardWidget(Config config, MessageBoard messageBoard) {
        this.messageBoard = messageBoard;
        messageBoardLayer = new MessageBoardLayer(messageBoard, 300);
        criticalMessageBoardLayer = new CriticalMessageBoardLayer(messageBoard, 480);
        guiElementMessages = new GuiElement.GuiElementBuilder()
                .setPosition(0.6f, -0.4f, -1f, 0.6f)
                .setAnchorPoint(GuiAnchorPoint.BOTTOM_LEFT)
                .setScale(config.getGraphicsSettings().getGuiScale())
                .setResolution(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight())
                .setHidden(!config.getGraphicsSettings().getShowDebugInfo())
                .addLayer(480, 300, messageBoardLayer)
                .build();
        guiElementCritical = new GuiElement.GuiElementBuilder()
                .setPosition(0.5f, -0.5f, -0.8f, 0.8f)
                .setAnchorPoint(GuiAnchorPoint.CENTER)
                .setScale(config.getGraphicsSettings().getGuiScale())
                .setResolution(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight())
                .setHidden(!config.getGraphicsSettings().getShowDebugInfo())
                .addLayer(480, 300, criticalMessageBoardLayer)
                .build();
    }

    @Override
    public void draw(Shader shader) {
        guiElementMessages.draw(shader);
        guiElementCritical.draw(shader);
    }

}
