package org.uav.input;

import java.io.IOException;

public class AxisBinding extends Binding {
    final int axis;
    final boolean inverse;
    final Float lowerBound;
    final Float upperBound;

    boolean axisPressed;

    public AxisBinding(Runnable action, int axis, boolean inverse, Float lowerBound, Float upperBound) throws IOException {
        super(action);
        this.axis = axis;
        this.inverse = inverse;
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
        else if(axisPressed && (lowerBound == null || axisValue <= lowerBound) && (upperBound == null || axisValue >= upperBound)) {
            axisPressed = false;
        }
    }
}