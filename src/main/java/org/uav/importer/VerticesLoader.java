package org.uav.importer;

import org.lwjgl.system.MemoryUtil;
import org.uav.model.Vertex;

import java.nio.FloatBuffer;
import java.util.List;

public class VerticesLoader {
    private final List<Vertex> vertices;

    public VerticesLoader(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public FloatBuffer loadToFloatBuffer() {
        FloatBuffer fb = MemoryUtil.memAllocFloat(Vertex.NUMBER_OF_FLOATS * vertices.size());
        vertices.forEach(vertex -> vertex.insertIntoFloatBuffer(fb));
        fb.flip();
        return fb;
    }
}
