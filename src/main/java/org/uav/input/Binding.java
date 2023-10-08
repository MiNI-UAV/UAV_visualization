package org.uav.input;

public class Binding {
    final public Runnable action;

    public Binding(Runnable action) {
        this.action = action;
    }
}
