package org.opengl.model;


import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.util.List;

public class IndicesLoader {
    private List<Integer> indices;

    public IndicesLoader(List<Integer> indices) {
        this.indices = indices;
    }

    public IntBuffer loadToIntBuffer() {
        IntBuffer intBuffer = MemoryUtil.memAllocInt(indices.size());
        indices.forEach(intBuffer::put);
        intBuffer.flip();
        return intBuffer;
    }
}
