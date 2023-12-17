package org.uav.logic.input.binding;

import lombok.Getter;

import java.io.IOException;

public class AxisBinding extends Binding {
    @Getter
    private final int axis;
    private final Float lowerBound;
    private final Float upperBound;
    private boolean axisPressed;

    public AxisBinding(Runnable action, int axis, Float lowerBound, Float upperBound) throws IOException {
        super(action);
        this.axis = axis;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        if(lowerBound == null && upperBound == null)
            throw new IOException("At least one of the following must be defined: lowerBound, upperBound.");
    }

    public void execute(float axisValue) {
        if(!axisPressed && (lowerBound == null || axisValue >= lowerBound) && (upperBound == null || axisValue <= upperBound)) {
            axisPressed = true;
            super.action.run();
        }
        else if(axisPressed && ((lowerBound != null && axisValue <= lowerBound) || (upperBound != null && axisValue >= upperBound))) {
            axisPressed = false;
        }
    }
}