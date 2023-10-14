package org.uav.queue;

import lombok.Value;

@Value
public class ActionReplyMessage {
    Integer response;
    Integer id;
}