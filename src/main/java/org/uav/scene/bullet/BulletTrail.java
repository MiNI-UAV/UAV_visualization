package org.uav.scene.bullet;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.uav.scene.shader.Shader;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class BulletTrail {
    private static final int POINTS_COUNT = 20;
    private int VAO;
    private final List<Vector3f> trailPoints;
    private final Shader bulletTrailShader;
    private final int emptyVertexArray;

    public BulletTrail(Shader bulletTrailShader) {
        trailPoints = new ArrayList<>();
        this.bulletTrailShader = bulletTrailShader;
        emptyVertexArray = glGenVertexArrays();
        setupModel();
    }

    private void setupModel() {
        VAO = GL30.glGenVertexArrays();
        int VBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, FloatBuffer.allocate(0), GL_STATIC_DRAW);
        glBindVertexArray(0);
    }

    public void addPoint(Vector3f point) {
        trailPoints.add(point);
        if(trailPoints.size() > POINTS_COUNT)
            trailPoints.remove(0);
    }

    public void draw(MemoryStack stack) {
        bulletTrailShader.use();
        bulletTrailShader.setVec3Array(stack, "trailPoints", trailPoints);
        bulletTrailShader.setInt("pointCount", trailPoints.size());

        glBindVertexArray(VAO);
        glDrawArrays(GL_POINTS, 0, 1);
        glBindVertexArray(0);
    }
}
