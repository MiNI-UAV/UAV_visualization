package org.uav.presentation.entity.vector;

import lombok.Setter;
import org.joml.Matrix3x2f;
import org.joml.Vector4f;
import org.uav.presentation.model.importer.IndicesLoader;
import org.uav.presentation.model.importer.VerticesLoader;
import org.uav.presentation.rendering.Shader;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public abstract class VectorShape {
    protected final List<Integer> indices;
    protected final int VAO;
    protected final Shader vectorShader;
    @Setter
    protected Matrix3x2f transform;
    protected Vector4f color;
    @Setter Vector4f cropRectangle;

    public VectorShape(List<VectorVertex> points, List<Integer> indices, Shader vectorShader) {
        this.vectorShader = vectorShader;
        this.indices = indices;
        VAO = loadPrimitives(points);
        transform = new Matrix3x2f();
        color = new Vector4f(Color.WHITE.getComponents(new float[4]));
        cropRectangle = new Vector4f(-1,-1,2,2);
    }

    public void setColor(Color color) {
        this.color = new Vector4f(color.getComponents(new float[4]));
    }

    private int loadPrimitives(List<VectorVertex> vertices) {
        final int VAO;
        IndicesLoader indicesLoader = new IndicesLoader(indices);
        VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, VerticesLoader.loadToFloatBuffer(vertices), GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesLoader.loadToIntBuffer(), GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, true, VectorVertex.NUMBER_OF_FLOATS * 4, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
        return VAO;
    }
}
