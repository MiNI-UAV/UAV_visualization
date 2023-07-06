package org.uav.model.status;

import org.uav.model.status.ProjectileStatus;

import java.util.HashMap;
import java.util.Map;

public class ProjectileStatuses {
    public Map<Integer, ProjectileStatus> map;

    public ProjectileStatuses() {
        map = new HashMap<>();
    }
}
