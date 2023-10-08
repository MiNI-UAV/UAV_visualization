package org.uav.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.uav.model.controlMode.ControlModeReply;

import java.util.List;
import java.util.Map;


@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class AvailableControlModes {
    Map<String, ControlModeReplyList> modes;

    @Value
    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    public static class ControlModeReplyList {
        List<ControlModeReply> reply;
    }
}
