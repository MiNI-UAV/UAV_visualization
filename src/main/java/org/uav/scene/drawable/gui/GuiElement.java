package org.uav.scene.drawable.gui;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.uav.model.Vertex;
import org.uav.scene.shader.Shader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GuiElement {
    // TODO [Mu-84]: Proper GUI scaling on different window resolutions.
    private final List<GuiLayer> layers;
    private GuiAnchorPoint anchorPoint;
    private float scale;
    private float resolution;
    private int overlayLevel;
    private boolean hidden;
    private float[] vertexCoords;

    private GuiElement() {
        anchorPoint = GuiAnchorPoint.NONE;
        scale = 1.0f;
        resolution = 1.0f;
        hidden = false;
        layers = new ArrayList<>();
        overlayLevel = 0;
    }

    public void draw(Shader shader) {
        if(hidden) return;
        layers.forEach(layer -> layer.draw(shader));
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    public static class GuiElementBuilder {

        GuiElement guiElement;

        public GuiElementBuilder() {
            guiElement = new GuiElement();
        }

        public GuiElementBuilder setPosition(float top, float bottom, float left, float right) {
            guiElement.vertexCoords = new float[] {
                    right, top,
                    right, bottom,
                    left,  bottom,
                    left,  top
            };
            return this;
        }

        public GuiElementBuilder setAnchorPoint(GuiAnchorPoint guiAnchorPoint) {
            guiElement.anchorPoint = guiAnchorPoint;
            return this;
        }

        public GuiElementBuilder setScale(float guiScale) {
            guiElement.scale = guiScale;
            return this;
        }

        public GuiElementBuilder setHidden(boolean hidden) {
            guiElement.hidden = hidden;
            return this;
        }

        public GuiElementBuilder setOverlayLevel(int overlayLevel) {
            guiElement.overlayLevel = overlayLevel;
            return this;
        }

        public GuiElement build() {
            return guiElement;
        }

        public GuiElementBuilder addLayer(BufferedImage texture) {
            var vertices = constructVertexList(guiElement.layers.size());
            guiElement.layers.add(new GuiLayer(vertices, texture));
            return this;
        }

        public GuiElementBuilder addLayer(int width, int height, DrawableGuiLayer guiLayer) {
            var vertices = constructVertexList(guiElement.layers.size());
            guiElement.layers.add(new GuiLayer(vertices, width, height, guiLayer));
            return this;
        }

        private List<Vertex> constructVertexList(int layerNo) {
            float[] v = guiElement.vertexCoords;
            float z = -guiElement.overlayLevel * 0.1f - layerNo * 0.01f;
            return List.of(
                    new Vertex(
                            new Vector3f(v[0], v[1], z),
                            new Vector3f(0,0,0),
                            new Vector2f(1,0)
                    ),
                    new Vertex(
                            new Vector3f(v[2], v[3], z),
                            new Vector3f(0,0,0),
                            new Vector2f(1, 1)
                    ),
                    new Vertex(
                            new Vector3f(v[4], v[5], z),
                            new Vector3f(0,0,0),
                            new Vector2f(0, 1)
                    ),
                    new Vertex(
                            new Vector3f(v[6], v[7], -layerNo * 0.01f),
                            new Vector3f(0,0,0),
                            new Vector2f(0, 0)
                    )
            );
        }

        public GuiElementBuilder setResolution(int windowWidth, int windowHeight) {
            guiElement.resolution = (float) windowWidth / windowHeight;
            return this;
        }
    }
}
