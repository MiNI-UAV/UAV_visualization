package org.uav.presentation.entity.vector;

import org.joml.Vector2f;
import org.uav.presentation.rendering.Shader;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class VectorCircle extends VectorShape {
    public VectorCircle(Vector2f center, float radius, int cornerCount, Shader vectorShader) {
        super(calculateCirclePoints(center, radius, cornerCount), getCircleIndices(cornerCount), vectorShader);
    }

    private static List<Integer> getCircleIndices(int cornerCount) {
        List<Integer> indices = new ArrayList<>();
        for(int i = 2; i < cornerCount + 1; ++i)
        {
            indices.add(0);
            indices.add(i-1);
            indices.add(i);
        }
        indices.add(0);
        indices.add(cornerCount);
        indices.add(1);
        return indices;
    }

    private static List<VectorVertex> calculateCirclePoints(Vector2f center, float radius, int cornerCount) {
        if(cornerCount < 3) throw new IllegalArgumentException("Tried to create circle with less than 3 corners");
        List<VectorVertex> vertices = new ArrayList<>();
        vertices.add(new VectorVertex(center));
        float angle = 2 * (float) Math.PI / cornerCount;
        for(int i = 0; i < cornerCount; i++) {
            float sinAngle = (float) Math.sin(angle * i);
            float cosAngle = (float) Math.cos(angle * i);
            vertices.add(new VectorVertex(center.x + radius * cosAngle, center.y + radius * sinAngle));
        }
        return vertices;
    }

    public void draw() {
        vectorShader.use();
        vectorShader.setVec4("color", color);
        vectorShader.setMatrix3x2f("transform", transform);
        vectorShader.setVec4("cropRectangle", cropRectangle);

        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLE_FAN, indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
