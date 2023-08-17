package org.uav.queue;

public enum Actions {
    drop,
    shoot;

    public String toMessage() {
        String s = "";
        switch (this)
        {
            case drop -> s = "drop";
            case shoot -> s = "shot";
        }
        return s;
    }
}


