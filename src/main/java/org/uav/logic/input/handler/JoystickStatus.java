package org.uav.logic.input.handler;

import java.util.ArrayList;
import java.util.List;

public class JoystickStatus {
    public List<Float> axes;

    public JoystickStatus() {
        axes = new ArrayList<>();
    }
}
