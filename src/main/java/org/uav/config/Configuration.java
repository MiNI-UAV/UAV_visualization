 package org.uav.config;

 import org.uav.input.CameraType;
 import org.uav.input.JoystickButtonFunctions;
 import org.uav.queue.Actions;

 import java.util.Map;

public class Configuration {
    public Map<Integer, Integer> joystickMapping;
    public Map<Integer, Boolean> joystickInversionMapping;
    public Map<Integer, JoystickButtonFunctions> joystickButtonsMapping;
    public Map<Integer, Actions> axisActionsMapping;
    public String address = "127.0.0.1";
    //public String address = "83.6.113.220";
    public String droneName = "Wojtek";
    public float deadZoneFactor = 0.2f;
    public CameraType type = CameraType.HorizontalCamera;
}
