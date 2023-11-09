package org.uav.importer;

import org.lwjgl.system.MemoryUtil;
import org.uav.model.ShaderVertex;

import java.nio.FloatBuffer;
import java.util.List;

public class VerticesLoader {

    public static <TVertex extends ShaderVertex> FloatBuffer loadToFloatBuffer(List<? extends TVertex> vertices) {
        FloatBuffer fb = MemoryUtil.memAllocFloat(vertices.get(0).getNumberOfFloats() * vertices.size());
        vertices.forEach(vertex -> vertex.insertIntoFloatBuffer(fb));
        fb.flip();
        return fb;
    }
}
