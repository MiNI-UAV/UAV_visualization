package org.uav.model.status;

public class JoystickStatus {
    public int[] rawData;

    public JoystickStatus(int size) {
        rawData = new int[size];
    }
}
