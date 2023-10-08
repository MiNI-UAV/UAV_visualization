package org.uav.queue;

public enum Actions {
    drop,
    shot;

    public String toMessage() {
        String s = "";
        switch (this)
        {
            case drop -> s = "drop";
            case shot -> s = "shot";
        }
        return s;
    }
}


