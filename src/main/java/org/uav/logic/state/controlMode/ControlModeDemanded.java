package org.uav.logic.state.controlMode;

import java.util.Map;

public class ControlModeDemanded {
    public final String name;
    public final Map<ControlModeReply, Float> demanded;

    public ControlModeDemanded(String name, Map<ControlModeReply, Float> demanded) {
        this.name = name;
        this.demanded = demanded;
    }
}
