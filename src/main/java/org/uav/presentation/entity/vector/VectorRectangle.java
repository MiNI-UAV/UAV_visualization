package org.uav.presentation.entity.vector;

import org.uav.presentation.rendering.Shader;

import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class VectorRectangle extends VectorShape{
    private static final int QUAD_POINT_COUNT = 4;

    public VectorRectangle(List<VectorVertex> points, Shader vectorShader) {
        super(checkPointCount(points), getQuadIndices(), vectorShader);
    }

    private static List<VectorVertex> checkPointCount(List<VectorVertex> points) {
        if(points.size() != QUAD_POINT_COUNT) throw new IllegalArgumentException("Tried to create quad vector shape with incorrect number of points.");
        return points;
    }

    public static List<Integer> getQuadIndices() {
        return List.of(
                0, 1, 2,   // first triangle
                2, 3, 0    // second triangle
        );
    }

    public void draw() {
        vectorShader.use();
        vectorShader.setVec4("color", color);
        vectorShader.setMatrix3x2f("transform", transform);
        vectorShader.setVec4("cropRectangle", cropRectangle);

        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
