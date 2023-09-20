package org.uav.queue;

public enum ControlMode {
    Acrobatic, Angle, Positional, None;

    public String toMessage() {
        String s = "";
        switch (this)
        {
            case Acrobatic -> s = "QACRO";
            case Angle -> s = "QANGLE";
            case Positional -> s = "QPOS";
            case None -> s = "NONE";
        }
        return s;
    }
}

