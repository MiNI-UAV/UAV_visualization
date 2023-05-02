package org.uav.model;


public class Texture {
    private final int id;
    private final String type;

    public Texture(int id, String type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
