package org.uav.presentation.entity.vector;

import lombok.Setter;
import org.joml.Vector2f;
import org.uav.presentation.rendering.Shader;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class VectorCircleArc extends VectorShape {
    private final Shader circleArcShader;
    @Setter
    private float startAngle;
    @Setter
    private float arcAngle;
    private Vector2f center;
    public VectorCircleArc(Vector2f center, float radius, int cornerCount, Shader vectorShader, Shader circleArcShader) {
        super(calculateCirclePoints(center, radius, cornerCount), getCircleIndices(cornerCount), vectorShader);
        this.circleArcShader = circleArcShader;
        this.center = center;
        startAngle = 0;
        arcAngle = (float) Math.PI;
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
        circleArcShader.use();
        circleArcShader.setVec4("color", color);
        circleArcShader.setMatrix3x2f("transform", transform);
        circleArcShader.setVec4("cropRectangle", cropRectangle);
        circleArcShader.setFloat("startAngle", startAngle);
        circleArcShader.setFloat("arcAngle", arcAngle);
        circleArcShader.setVec2("center", center);

        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLE_FAN, indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
}
