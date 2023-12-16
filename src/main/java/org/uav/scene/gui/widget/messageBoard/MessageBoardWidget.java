package org.uav.scene.gui.widget.messageBoard;

import org.uav.config.Config;
import org.uav.messages.MessageBoard;
import org.uav.scene.GuiWidget;
import org.uav.scene.gui.GuiAnchorPoint;
import org.uav.scene.gui.GuiElement;
import org.uav.scene.gui.widget.messageBoard.layers.CriticalMessageBoardLayer;
import org.uav.scene.gui.widget.messageBoard.layers.MessageBoardLayer;
import org.uav.scene.shader.Shader;

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
