package org.uav.queue;

public enum Actions {
    drop,
    shot,
    release;

    public String toMessage() {
        String s = "";
        switch (this)
        {
            case drop -> s = "drop";
            case shot -> s = "shot";
            case release -> s = "release";
        }
        return s;
    }
}


