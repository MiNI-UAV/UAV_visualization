package org.uav.scene.gui.widget.projectiles;

import org.joml.Vector2i;
import org.uav.model.Projectile;
import org.uav.model.SimulationState;
import org.uav.scene.gui.DrawableGuiLayer;

import java.awt.*;
import java.text.MessageFormat;

public class ProjectileLayer implements DrawableGuiLayer {
    private static final int FONT_SIZE = 30;

    private String currentAmmoName;
    private int currentAmmoAmount;
    private float currentAmmoReloadPercentage;
    private String currentCargoName;
    private int currentCargoAmount;
    private float currentCargoReloadPercentage;

    public ProjectileLayer() {
    }

    public void update(SimulationState simulationState) {
        Projectile ammo = simulationState.getAmmos().get(simulationState.getCurrentlyChosenAmmo());
        currentAmmoName = ammo.name;
        currentAmmoAmount = ammo.currentAmount;
        currentAmmoReloadPercentage = ammo.timeToReload / ammo.reloadTimeS;

        Projectile cargo = simulationState.getCargos().get(simulationState.getCurrentlyChosenCargo());
        currentCargoName = cargo.name;
        currentCargoAmount = cargo.currentAmount;
        currentCargoReloadPercentage = cargo.timeToReload / cargo.reloadTimeS ;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
        g.setColor(Color.white);

        String ammoString = MessageFormat.format("{0} x{1}", currentAmmoName, currentAmmoAmount);
        String cargoString = MessageFormat.format("{0} x{1}", currentCargoName, currentCargoAmount);
        int fontHeight = g.getFontMetrics().getHeight();
        Vector2i ammoCircleCenter = new Vector2i(25, (int) (100 - 0.25f*fontHeight));
        Vector2i cargoCircleCenter = new Vector2i(25, (int) (100 + 0.75f*fontHeight));
        int fullCircleRadius = 20;
        int ammoRadius = (int) (fullCircleRadius * currentAmmoReloadPercentage);
        int cargoRadius = (int) (fullCircleRadius * currentCargoReloadPercentage);
        g.fillOval(ammoCircleCenter.x - ammoRadius, ammoCircleCenter.y - ammoRadius, 2 * ammoRadius, 2 * ammoRadius);
        g.drawString(ammoString, 50, 100);
        g.fillOval(cargoCircleCenter.x - cargoRadius, cargoCircleCenter.y - cargoRadius, 2 * cargoRadius, 2 * cargoRadius);
        g.drawString(cargoString, 50, 100 + fontHeight);
    }
}
