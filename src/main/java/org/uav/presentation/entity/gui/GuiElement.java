package org.uav.presentation.entity.gui;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.uav.presentation.model.ModelVertex;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GuiElement {
    // TODO [Mu-84]: Proper GUI scaling on different window resolutions.
    private final List<GuiLayer> layers;
    private GuiAnchorPoint anchorPoint;
    private float scale;
    private float resolutionRatio;
    private boolean horizontalResolution;
    private int overlayLevel;
    private boolean hidden;

    private GuiElement() {
        anchorPoint = GuiAnchorPoint.NONE;
        scale = 1.0f;
        resolutionRatio = 1.0f;
        horizontalResolution = true;
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
        Vector4f positions;

        public GuiElementBuilder() {
            guiElement = new GuiElement();
        }

        public GuiElementBuilder setPosition(float top, float bottom, float left, float right) {
            positions = new Vector4f(top, bottom, left, right);
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

        private Vector4f scaleGui(float top, float bottom, float left, float right) {
            float ratio = guiElement.resolutionRatio;
            float scale = guiElement.scale;
            return switch(guiElement.anchorPoint) {
                case CENTER -> guiElement.horizontalResolution ?
                        new Vector4f(top, bottom, left / ratio, right / ratio).mul(scale):
                        new Vector4f(top * ratio, bottom * ratio, left, right).mul(scale);
                case BOTTOM_RIGHT -> guiElement.horizontalResolution ?
                        new Vector4f(bottom - (bottom - top) * scale, bottom, right - (right - left) / ratio * scale, right):
                        new Vector4f(bottom - (bottom - top) * ratio * scale, bottom, right - (right - left) * scale, right);
                case BOTTOM -> guiElement.horizontalResolution ?
                        new Vector4f(bottom - (bottom - top) * scale, bottom, left / ratio * scale, right / ratio * scale):
                        new Vector4f(bottom - (bottom - top) * ratio * scale, bottom, left * scale, right * scale);
                case BOTTOM_FULL -> guiElement.horizontalResolution ?
                        new Vector4f(bottom - (bottom - top) * scale, bottom, left, right):
                        new Vector4f(bottom - (bottom - top) * ratio * scale, bottom, left, right);
                case BOTTOM_LEFT -> guiElement.horizontalResolution ?
                        new Vector4f(bottom - (bottom - top) * scale, bottom, left , left + (right - left) / ratio * scale):
                        new Vector4f(bottom - (bottom - top) * ratio * scale, bottom, left, left + (right - left) * scale);
                case TOP_RIGHT -> guiElement.horizontalResolution ?
                        new Vector4f(top, top - (top - bottom) * scale, right - (right - left) / ratio * scale, right):
                        new Vector4f(top, top - (top - bottom) * ratio * scale, right - (right - left) * scale, right);
                case TOP_LEFT -> guiElement.horizontalResolution ?
                        new Vector4f(top, top - (top - bottom) * scale, left , left + (right - left) / ratio * scale):
                        new Vector4f(top, top - (top - bottom) * ratio * scale, left, left + (right - left) * scale);
                case TOP -> guiElement.horizontalResolution ?
                        new Vector4f(top, top - (top - bottom) * scale, left / ratio * scale , right / ratio * scale):
                        new Vector4f(top, top - (top - bottom) * ratio * scale, left * scale, right * scale);
                case LEFT -> guiElement.horizontalResolution ?
                        new Vector4f(top * scale, bottom * scale, left , left + (right - left) / ratio * scale):
                        new Vector4f(top * scale * ratio, bottom * ratio * scale, left, left + (right - left) * scale);
                case RIGHT, TOP_FULL, LEFT_FULL, RIGHT_FULL -> throw new RuntimeException("Not implemented");
                default -> new Vector4f(top, bottom, left, right);
            };
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

        private List<ModelVertex> constructVertexList(int layerNo) {
            Vector4f scaledDim = scaleGui(positions.x, positions.y, positions.z, positions.w);
            float[] v = new float[] {
                    scaledDim.w, scaledDim.x,
                    scaledDim.w, scaledDim.y,
                    scaledDim.z, scaledDim.y,
                    scaledDim.z, scaledDim.x
            };
            float z = -guiElement.overlayLevel * 0.1f - layerNo * 0.01f;
            return List.of(
                    new ModelVertex(
                            new Vector3f(v[0], v[1], z),
                            new Vector3f(0,0,0),
                            new Vector2f(1,0)
                    ),
                    new ModelVertex(
                            new Vector3f(v[2], v[3], z),
                            new Vector3f(0,0,0),
                            new Vector2f(1, 1)
                    ),
                    new ModelVertex(
                            new Vector3f(v[4], v[5], z),
                            new Vector3f(0,0,0),
                            new Vector2f(0, 1)
                    ),
                    new ModelVertex(
                            new Vector3f(v[6], v[7], -layerNo * 0.01f),
                            new Vector3f(0,0,0),
                            new Vector2f(0, 0)
                    )
            );
        }

        public GuiElementBuilder setResolution(int windowWidth, int windowHeight) {
            guiElement.resolutionRatio = (float) windowWidth / windowHeight;
            guiElement.horizontalResolution = windowWidth >= windowHeight;
            return this;
        }
    }
}
