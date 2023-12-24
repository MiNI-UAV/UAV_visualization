package org.uav.presentation.entity.gui.widget.projectiles;

import lombok.AllArgsConstructor;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.config.Config;
import org.uav.logic.state.projectile.Projectile;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.gui.GuiAnchorPoint;
import org.uav.presentation.entity.gui.Widget;
import org.uav.presentation.entity.sprite.Sprite;
import org.uav.presentation.entity.text.TextEngine;
import org.uav.presentation.entity.vector.VectorCircleArc;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;

public class ProjectileWidget extends Widget {

    private static final float FONT_SIZE_NORM = 40f / 1080;
    private static final int RELOAD_CIRCLE_POINT_COUNT = 10;
    private LoadInfo chosenAmmo;
    private LoadInfo chosenCargo;

    private final Sprite backgroundSprite;
    private final VectorCircleArc reloadShape;
    private final TextEngine textEngine;


    public ProjectileWidget(BufferedImage background, Shader spriteShader, Shader vectorShader, Shader circleArcShader, Shader textShader, Config config) {
        super(getWidgetPosition(), GuiAnchorPoint.TOP_RIGHT, config);
        backgroundSprite = new Sprite(background, spriteShader);
        reloadShape = new VectorCircleArc(new Vector2f(), 0.1f, RELOAD_CIRCLE_POINT_COUNT, vectorShader, circleArcShader);
        textEngine = new TextEngine(getScaledPosition(), FONT_SIZE_NORM, textShader, config);
    }

    private static Vector4f getWidgetPosition() {
        return new Vector4f(1f, 0.8f, 0.6f, 1f);
    }

    public void update(SimulationState simulationState) {
        if(simulationState.getCurrentlyChosenAmmo() < simulationState.getAmmos().size()) {
            Projectile ammo = simulationState.getAmmos().get(simulationState.getCurrentlyChosenAmmo());
            chosenAmmo = new LoadInfo(ammo.name, ammo.currentAmount, ammo.timeToReload / ammo.reloadTimeS);
        } else chosenAmmo = null;

        if(simulationState.getCurrentlyChosenCargo() < simulationState.getCargos().size()) {
            Projectile cargo = simulationState.getCargos().get(simulationState.getCurrentlyChosenCargo());
            chosenCargo = new LoadInfo(cargo.name, cargo.currentAmount, cargo.timeToReload / cargo.reloadTimeS);
        } else chosenCargo = null;
    }

    @Override
    protected void drawWidget(MemoryStack stack) {
        backgroundSprite.draw(stack);

        float scale = (getScaledPosition().x - getScaledPosition().y) / (getScaledPosition().w - getScaledPosition().z);
        if(chosenAmmo != null) {
            String ammoString = MessageFormat.format("{0} x{1}", chosenAmmo.name, chosenAmmo.amount);
            float ammoRatio = (float) (2 * Math.PI * chosenAmmo.reloadPercentage);
            var transform = new Matrix3x2f();
            transform.translate(-0.8f, 0.2f + textEngine.getFontHeight() * 0.25f);
            transform.scale(scale, 1);
            reloadShape.setTransform(transform);
            reloadShape.setArcAngle(ammoRatio);
            reloadShape.draw(stack);
            textEngine.setPosition(-0.7f, 0.2f);
            textEngine.renderText(ammoString);
        }

        if(chosenCargo != null) {
            String cargoString = MessageFormat.format("{0} x{1}", chosenCargo.name, chosenCargo.amount);
            float cargoRatio = (float) (2 * Math.PI * chosenCargo.reloadPercentage);
            var transform = new Matrix3x2f();
            transform.translate(-0.8f, -0.2f + textEngine.getFontHeight() * 0.25f);
            transform.scale(scale, 1);
            reloadShape.setTransform(transform);
            reloadShape.setArcAngle(cargoRatio);
            reloadShape.draw(stack);
            textEngine.setPosition(-0.7f, -0.2f);
            textEngine.renderText(cargoString);
        }
    }

    @AllArgsConstructor
    private static class LoadInfo {
        private String name;
        private int amount;
        private float reloadPercentage;
    }
}
