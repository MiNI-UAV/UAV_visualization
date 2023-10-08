package org.uav.input;

public class ButtonBinding extends Binding {
    public final int button;
    public final boolean inverse;

    public boolean buttonPressed;

    public ButtonBinding(Runnable action, int button, boolean inverse) {
        super(action);
        this.button = button;
        this.inverse = inverse;
        buttonPressed = false;
    }

    public void execute(byte b) {
        if(!buttonPressed && ((b == 1 && !inverse) || (b == 0 && inverse))) {
            buttonPressed = true;
            super.action.run();
        }
        else if(buttonPressed && ((b == 0 && !inverse) || (b == 1 && inverse)))
            buttonPressed = false;
    }
}
