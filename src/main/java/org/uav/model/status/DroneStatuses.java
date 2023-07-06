package org.uav.model.status;

import org.uav.model.status.DroneStatus;

import java.util.HashMap;
import java.util.Map;

public class DroneStatuses {
    public Map<Integer, DroneStatus> map;

    public DroneStatuses() {
        map = new HashMap<>();
    }
}
