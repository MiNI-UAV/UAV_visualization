package org.uav.logic.fps;

import lombok.Getter;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class FpsCounter {

    @Getter
    private float framesPerSecond;
    @Getter
    private float millisecondsPerFrame;
    private int frameCounter;
    private double lastTime;

    public FpsCounter() {
        framesPerSecond = 0;
        frameCounter = 0;
        lastTime = glfwGetTime();
    }

    public void nextFrame() {
        double currentTime = glfwGetTime();
        frameCounter++;
        if ( currentTime - lastTime >= 1.0 ){
            framesPerSecond = frameCounter*0.5f +  framesPerSecond*0.5f;
            millisecondsPerFrame = 1000 / (float) frameCounter;
            frameCounter = 0;
            lastTime += 1.0;
        }
    }


}
