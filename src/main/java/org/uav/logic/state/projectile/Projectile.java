package org.uav.logic.state.projectile;

import org.uav.logic.config.DroneParameters;
import org.uav.logic.state.simulation.SimulationState;

public class Projectile {
    public final SimulationState simulationState;
    public final String name;
    public final float reloadTimeS;
    public final int storageCapacity;

    public int currentAmount;
    public float lastFireTimestampS;
    public float  timeToReload;


    public Projectile(String name, float reloadTimeS, int storageCapacity, SimulationState simulationState) {
        this.simulationState = simulationState;
        this.name = name;
        this.reloadTimeS = reloadTimeS;
        this.storageCapacity = storageCapacity;
        currentAmount = storageCapacity;
        lastFireTimestampS = 0;
        timeToReload = 0;
    }

    public Projectile(DroneParameters.Projectile projectile, SimulationState simulationState) {
        this(projectile.getName(), projectile.getReload(), projectile.getAmount(), simulationState);
    }

    public void update() {
        timeToReload = reloadTimeS - (simulationState.getSimulationTimeS() - lastFireTimestampS);
        if(timeToReload <= 0) {
            timeToReload = 0;
        }
    }

    public void parseProjectileMessage(String message) {
        String[] s1 = message.split(";");
        if(!s1[0].equals("ok")) return;
        String[] s2 = s1[1].split(",");
        int result = Integer.parseInt(s2[0]);
        int id = Integer.parseInt(s2[1]);
        if(result != -1 && result != -2) {
            currentAmount = result;
            lastFireTimestampS = simulationState.getSimulationTimeS();
        }
    }

    public void reset() {
        currentAmount = storageCapacity;
        lastFireTimestampS = 0;
        timeToReload = 0;
    }
}
