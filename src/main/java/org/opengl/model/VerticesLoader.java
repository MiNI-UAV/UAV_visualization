package org.opengl.model;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;

public class VerticesLoader {
    private List<Vertex> vertices;

    public VerticesLoader(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public FloatBuffer loadToFloatBuffer() {
        FloatBuffer fb = MemoryUtil.memAllocFloat(Vertex.NUMBER_OF_FLOATS * vertices.size());
        vertices.stream().forEach(vertex -> vertex.insertIntoFloatBuffer(fb));
        fb.flip();
        return fb;
    }
}
