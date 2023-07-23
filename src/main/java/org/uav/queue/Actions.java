package org.uav.queue;

public enum Actions {
    drop,
    shoot;

    @Override
    public String toString() {
        String s = "";
        switch (this)
        {
            case drop -> s = "drop";
            case shoot -> s = "shot";
        }
        return s;
    }
}


