package org.uav.input;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyboardBinding extends Binding {
    public final int keyCode;
    private boolean keyPressed;

    public KeyboardBinding(Runnable action, int key) {
        super(action);
        keyCode = key;
        keyPressed = false;
    }

    public void execute(int action) {
        if(!keyPressed && action == GLFW_PRESS) {
            keyPressed = true;
            super.action.run();
        }
        else if(keyPressed && action == GLFW_RELEASE)
            keyPressed = false;
    }
}
