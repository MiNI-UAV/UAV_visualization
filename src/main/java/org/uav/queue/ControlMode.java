package org.uav.queue;

public enum ControlMode {
    Acrobatic, Angle, Positional, None;

    public String toMessage() {
        String s = "";
        switch (this)
        {
            case Acrobatic -> s = "acro";
            case Angle -> s = "angle";
            case Positional -> s = "pos";
            case None -> s = "none";
        }
        return s;
    }
}

