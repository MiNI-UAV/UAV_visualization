package org.uav.queue;

public enum Actions {
    shot;

    @Override
    public String toString() {
        String s = "";
        switch (this)
        {
            case shot -> s = "shot";
        }
        return s;
    }
}


