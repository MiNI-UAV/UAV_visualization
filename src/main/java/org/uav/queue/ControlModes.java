package org.uav.queue;

public enum ControlModes {
    acro, angle, pos, none;

    @Override
    public String toString() {
        String s = "";
        switch (this)
        {
            case acro -> s = "acro";
            case angle -> s = "angle";
            case pos -> s = "pos";
            case none -> s = "none";
        }
        return s;
    }
}

