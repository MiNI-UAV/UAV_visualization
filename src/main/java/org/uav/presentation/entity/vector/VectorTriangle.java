package org.uav.presentation.entity.vector;

import org.lwjgl.system.MemoryStack;
import org.uav.presentation.rendering.Shader;

import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class VectorTriangle extends VectorShape {
    private static final int TRIANGLE_POINT_COUNT = 3;

    public VectorTriangle(List<VectorVertex> points, Shader vectorShader) {
        super(checkPointCount(points), getTriangleIndices(), vectorShader);
    }

    private static List<VectorVertex> checkPointCount(List<VectorVertex> points) {
        if(points.size() != TRIANGLE_POINT_COUNT) throw new IllegalArgumentException("Tried to create triangle vector shape with incorrect number of points.");
        return points;
    }

    public static List<Integer> getTriangleIndices() {
        return List.of(0, 1, 2);
    }

    public void draw(MemoryStack stack) {
        vectorShader.use();
        vectorShader.setVec4("color", color);
        vectorShader.setMatrix3x2f(stack, "transform", transform);
        vectorShader.setVec4("cropRectangle", cropRectangle);

        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
