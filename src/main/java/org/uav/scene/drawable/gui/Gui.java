package org.uav.scene.drawable.gui;

import org.uav.scene.drawable.GuiWidget;
import org.uav.scene.shader.Shader;

import java.util.HashMap;
import java.util.Map;

public class Gui {
    private final Map<String, GuiWidget> guiElements;

    private Gui() {
        guiElements = new HashMap<>();
    }

    public GuiWidget guiWidget(String name) {
        return guiElements.get(name);
    }

    public void draw(Shader shader) {
        guiElements.forEach((name, element) -> element.draw(shader));
    }

    public void update() {
        guiElements.forEach((name, element) -> element.update());
    }

    public static class GuiBuilder {
        private final Gui gui;
        public GuiBuilder() {
            gui = new Gui();
        }

        public GuiBuilder addGuiElement(String name, GuiWidget guiElement) {
            gui.guiElements.put(name, guiElement);
            return this;
        }

        public Gui build() {
            return gui;
        }
    }
}
