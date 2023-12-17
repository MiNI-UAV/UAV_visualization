package org.uav.logic.input.binding;

public class Binding {
    final public Runnable action;

    public Binding(Runnable action) {
        this.action = action;
    }
}
