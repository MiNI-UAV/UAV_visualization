package org.uav.presentation.entity.gui;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.config.Config;

import static org.lwjgl.opengl.GL11C.glViewport;

public abstract class Widget {
    private final float windowRatio;
    private final float viewRatio;
    private final float scale;
    private final boolean horizontalResolution;
    private final GuiAnchorPoint anchorPoint;
    private final Vector4i viewportResolution;
    private final Vector2i originalResolution;
    @Getter
    private final Vector4f scaledPosition;
    @Setter @Getter
    private boolean isHidden;

    public Widget(Vector4f widgetPosition, GuiAnchorPoint anchorPoint, Config config) {
        this(widgetPosition, anchorPoint, config, false);
    }

    public Widget(Vector4f widgetPosition, GuiAnchorPoint anchorPoint, Config config, boolean isHidden) {
        this.anchorPoint = anchorPoint;
        this.isHidden = isHidden;
        int windowWidth = config.getGraphicsSettings().getWindowWidth();
        int windowHeight = config.getGraphicsSettings().getWindowHeight();
        originalResolution = new Vector2i(windowWidth, windowHeight);
        windowRatio = (float) windowWidth / windowHeight;
        viewRatio = (widgetPosition.x - widgetPosition.y) / (widgetPosition.w - widgetPosition.z);
        scale = config.getGraphicsSettings().getGuiScale();
        horizontalResolution = windowWidth >= windowHeight;
        scaledPosition = scaleGui(widgetPosition);
        viewportResolution = calculateViewportResolution(scaledPosition);
    }


    public void draw(MemoryStack stack) {
        if(!isHidden) {
            glViewport(viewportResolution.x, viewportResolution.y, viewportResolution.z, viewportResolution.w);
            drawWidget(stack);
            glViewport(0, 0, originalResolution.x, originalResolution.y);
        }
    }

    protected abstract void drawWidget(MemoryStack stack);

    protected float getViewRatio() {
        return viewRatio;
    }

    private Vector4i calculateViewportResolution(Vector4f pos) {
        return new Vector4i(
                (int) ((pos.z + 1) / 2 * originalResolution.x),
                (int) ((pos.y + 1) / 2 * originalResolution.y),
                (int) ((pos.w - pos.z) / 2 * originalResolution.x) + 1,
                (int) ((pos.x - pos.y) / 2 * originalResolution.y) + 1
        );
    }
    
    private Vector4f scaleGui(Vector4f position) {
        float top = position.x;
        float bottom = position.y;
        float left = position.z;
        float right = position.w;
        return switch(anchorPoint) {
            case CENTER -> horizontalResolution ?
                    new Vector4f(top, bottom, left / windowRatio, right / windowRatio).mul(scale):
                    new Vector4f(top * windowRatio, bottom * windowRatio, left, right).mul(scale);
            case BOTTOM_RIGHT -> horizontalResolution ?
                    new Vector4f(bottom - (bottom - top) * scale, bottom, right - (right - left) / windowRatio * scale, right):
                    new Vector4f(bottom - (bottom - top) * windowRatio * scale, bottom, right - (right - left) * scale, right);
            case BOTTOM -> horizontalResolution ?
                    new Vector4f(bottom - (bottom - top) * scale, bottom, left / windowRatio * scale, right / windowRatio * scale):
                    new Vector4f(bottom - (bottom - top) * windowRatio * scale, bottom, left * scale, right * scale);
            case BOTTOM_FULL -> horizontalResolution ?
                    new Vector4f(bottom - (bottom - top) * scale, bottom, left, right):
                    new Vector4f(bottom - (bottom - top) * windowRatio * scale, bottom, left, right);
            case BOTTOM_LEFT -> horizontalResolution ?
                    new Vector4f(bottom - (bottom - top) * scale, bottom, left , left + (right - left) / windowRatio * scale):
                    new Vector4f(bottom - (bottom - top) * windowRatio * scale, bottom, left, left + (right - left) * scale);
            case TOP_RIGHT -> horizontalResolution ?
                    new Vector4f(top, top - (top - bottom) * scale, right - (right - left) / windowRatio * scale, right):
                    new Vector4f(top, top - (top - bottom) * windowRatio * scale, right - (right - left) * scale, right);
            case TOP_LEFT -> horizontalResolution ?
                    new Vector4f(top, top - (top - bottom) * scale, left , left + (right - left) / windowRatio * scale):
                    new Vector4f(top, top - (top - bottom) * windowRatio * scale, left, left + (right - left) * scale);
            case TOP -> horizontalResolution ?
                    new Vector4f(top, top - (top - bottom) * scale, left / windowRatio * scale , right / windowRatio * scale):
                    new Vector4f(top, top - (top - bottom) * windowRatio * scale, left * scale, right * scale);
            case LEFT -> horizontalResolution ?
                    new Vector4f(top * scale, bottom * scale, left , left + (right - left) / windowRatio * scale):
                    new Vector4f(top * scale * windowRatio, bottom * windowRatio * scale, left, left + (right - left) * scale);
            case RIGHT, TOP_FULL, LEFT_FULL, RIGHT_FULL -> throw new RuntimeException("Not implemented");
            default -> new Vector4f(top, bottom, left, right);
        };
    }
}
