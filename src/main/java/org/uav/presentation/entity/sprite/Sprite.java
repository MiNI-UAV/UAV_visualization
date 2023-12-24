package org.uav.presentation.entity.sprite;

import lombok.Setter;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.lwjgl.system.MemoryUtil;
import org.uav.presentation.model.Texture;
import org.uav.presentation.model.importer.IndicesLoader;
import org.uav.presentation.model.importer.VerticesLoader;
import org.uav.presentation.rendering.Shader;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.uav.utils.OpenGLUtils.setupTexture;

public class Sprite implements AutoCloseable {
    private final int VAO;
    private final List<Integer> indices;
    private Texture texture;
    private Shader spriteShader;
    private FloatBuffer transform;
    @Setter
    private float opacity;

    public Sprite(BufferedImage texture, Shader spriteShader) {
        this(spriteShader);
        this.texture = setupTexture(texture);
    }

    public Sprite(int texture, Shader spriteShader) {
        this(spriteShader);
        this.texture = new Texture(texture, "", true);
    }

    private Sprite(Shader spriteShader) {
        this.spriteShader = spriteShader;
        indices = List.of(
                0, 1, 2,   // first triangle
                2, 3, 0    // second triangle
        );
        var vertices = List.of(
                new SpriteVertex(new Vector2f(-1,  1), new Vector2f(0, 0)),
                new SpriteVertex(new Vector2f( 1,  1), new Vector2f(1, 0)),
                new SpriteVertex(new Vector2f( 1, -1), new Vector2f(1, 1)),
                new SpriteVertex(new Vector2f(-1, -1), new Vector2f(0, 1))
        );
        VAO = loadPrimitives(vertices);
        transform = MemoryUtil.memCallocFloat(6);
        new Matrix3x2f().get(transform);
        opacity = 1.0f;
    }

    public void setTransform(Matrix3x2f transform) {
        transform.get(this.transform);
    }

    public void draw() {
        spriteShader.use();
        spriteShader.setMatrix3x2f("transform", transform);
        spriteShader.setFloat("opacity", opacity);

        glActiveTexture(GL_TEXTURE0);
        spriteShader.setInt("sprite", 0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    private int loadPrimitives(List<SpriteVertex> vertices) {
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

        glVertexAttribPointer(0, 2, GL_FLOAT, true, SpriteVertex.NUMBER_OF_FLOATS * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, SpriteVertex.NUMBER_OF_FLOATS * 4, 2 * 4);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
        return VAO;
    }

    @Override
    public void close() throws Exception {
        MemoryUtil.memFree(transform);
    }
}
