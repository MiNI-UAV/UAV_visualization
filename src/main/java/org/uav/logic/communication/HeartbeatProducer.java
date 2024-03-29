package org.uav.logic.communication;

import org.uav.logic.config.Config;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class HeartbeatProducer {

    static private final String HEART_BEAT_MSG = "beep";
    static private final int MILLISECONDS_IN_SECOND = 1000;

    private final Config config;
    private float lastHeartBeatTimeStampMs;
    public HeartbeatProducer(Config config) {
        this.config = config;
        lastHeartBeatTimeStampMs = 0;
    }

    public void sustainHeartBeat(DroneCommunication drone) {
        float currentTimeMs = (float) glfwGetTime() * MILLISECONDS_IN_SECOND;
        if(currentTimeMs - lastHeartBeatTimeStampMs > config.getServerSettings().getHeartBeatIntervalMs()) {
            lastHeartBeatTimeStampMs = currentTimeMs;
            drone.sendUtilsCommand(HEART_BEAT_MSG);
        }
    }

}
