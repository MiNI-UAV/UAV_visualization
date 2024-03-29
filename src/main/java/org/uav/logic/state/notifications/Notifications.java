package org.uav.logic.state.notifications;

import org.uav.presentation.entity.rope.Rope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notifications {
    public Map<Integer, String> droneModelsNames;
    public Map<Integer, String> projectileModelsNames;
    public List<Rope> ropes;

    public Notifications() {
        droneModelsNames = new HashMap<>();
        projectileModelsNames = new HashMap<>();
        ropes = new ArrayList<>();
    }
}
