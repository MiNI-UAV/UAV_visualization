package org.uav.input;

import static org.lwjgl.glfw.GLFW.*;

public class KeyboardBinding extends Binding {
    public final int keyCode;
    private boolean keyPressed;

    public KeyboardBinding(Runnable action, char key) {
        super(action);
        if(Character.isAlphabetic(key))
            keyCode = GLFW_KEY_A + (key - 'a');
        else if(Character.isDigit(key))
            keyCode = GLFW_KEY_0 + (key - '0');
        else
            keyCode = -1;
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
