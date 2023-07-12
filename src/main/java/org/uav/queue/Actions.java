package org.uav.queue;

public enum Actions {
    shoot;

    @Override
    public String toString() {
        String s = "";
        switch (this)
        {
            case shoot -> s = "shoot";
        }
        return s;
    }
}


