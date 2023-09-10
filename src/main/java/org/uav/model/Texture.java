package org.uav.model;


import lombok.Getter;

@Getter
public class Texture {
    private final int id;
    private final String type;
    private final boolean transparent;

    public Texture(int id, String type, boolean transparent) {
        this.id = id;
        this.type = type;
        this.transparent = transparent;
    }
}
