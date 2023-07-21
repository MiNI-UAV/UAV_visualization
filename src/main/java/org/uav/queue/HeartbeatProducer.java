package org.uav.queue;

import org.uav.config.Config;
import org.uav.model.Drone;

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

    public void sustainHeartBeat(Drone drone) {
        float currentTimeMs = (float) glfwGetTime() * MILLISECONDS_IN_SECOND;
        if(currentTimeMs - lastHeartBeatTimeStampMs > config.heartBeatIntervalMs) {
            lastHeartBeatTimeStampMs = currentTimeMs;
            drone.sendUtilsCommand(HEART_BEAT_MSG);
        }
    }

}
