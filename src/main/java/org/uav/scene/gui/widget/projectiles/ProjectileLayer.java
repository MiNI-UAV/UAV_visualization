package org.uav.scene.gui.widget.projectiles;

import lombok.AllArgsConstructor;
import org.joml.Vector2i;
import org.uav.model.Projectile;
import org.uav.model.SimulationState;
import org.uav.scene.gui.DrawableGuiLayer;

import java.awt.*;
import java.text.MessageFormat;

public class ProjectileLayer implements DrawableGuiLayer {
    private static final int FONT_SIZE = 30;

    @AllArgsConstructor
    private static class LoadInfo {
        private String name;
        private int amount;
        private float reloadPercentage;
    }

    private LoadInfo chosenAmmo;
    private LoadInfo chosenCargo;

    public ProjectileLayer() {
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
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE));
        g.setColor(Color.white);
        int fontHeight = g.getFontMetrics().getHeight();
        int fullCircleRadius = 20;

        if(chosenAmmo != null) {
            String ammoString = MessageFormat.format("{0} x{1}", chosenAmmo.name, chosenAmmo.amount);
            g.drawString(ammoString, 50, 100);

            Vector2i ammoCircleCenter = new Vector2i(25, (int) (100 - 0.25f*fontHeight));
            int ammoRadius = (int) (fullCircleRadius * chosenAmmo.reloadPercentage);
            g.fillOval(ammoCircleCenter.x - ammoRadius, ammoCircleCenter.y - ammoRadius, 2 * ammoRadius, 2 * ammoRadius);
        }

        if(chosenCargo != null) {
            String cargoString = MessageFormat.format("{0} x{1}", chosenCargo.name, chosenCargo.amount);
            g.drawString(cargoString, 50, 100 + fontHeight);

            Vector2i cargoCircleCenter = new Vector2i(25, (int) (100 + 0.75f*fontHeight));
            int cargoRadius = (int) (fullCircleRadius * chosenCargo.reloadPercentage);
            g.fillOval(cargoCircleCenter.x - cargoRadius, cargoCircleCenter.y - cargoRadius, 2 * cargoRadius, 2 * cargoRadius);
        }
    }
}
