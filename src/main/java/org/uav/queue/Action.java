package org.uav.queue;

public enum Action {
    drop,
    shoot,
    release;

    public String toMessage() {
        String s = "";
        switch (this)
        {
            case drop -> s = "drop";
            case shoot -> s = "shoot";
            case release -> s = "release";
        }
        return s;
    }
}


