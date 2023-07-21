package org.uav.model.status;

import java.util.HashMap;
import java.util.Map;

public class ProjectileStatuses {
    public Map<Integer, ProjectileStatus> map;

    public ProjectileStatuses() {
        map = new HashMap<>();
    }

    public ProjectileStatuses(Map<Integer, ProjectileStatus> map) {
        this.map = map;
    }
}
