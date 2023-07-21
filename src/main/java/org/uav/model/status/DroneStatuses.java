package org.uav.model.status;

import java.util.HashMap;
import java.util.Map;

public class DroneStatuses {
    public Map<Integer, DroneStatus> map;

    public DroneStatuses() {
        map = new HashMap<>();
    }

    public DroneStatuses(Map<Integer, DroneStatus> map) {
        this.map = map;
    }
}
