package org.opengl.model;

import org.opengl.shader.Shader;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {
    private final List<Vertex> vertices;
    private final List<Integer> indices;
    private final List<Texture> textures;
    private final Material material;
    private int VAO;
    private int VBO;
    private int EBO;

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public List<Texture> getTextures() {
        return textures;
    }

    public Mesh(List<Vertex> vertices, List<Integer> indices, List<Texture> textures, Material material) {
        this.vertices = vertices;
        this.indices = indices;
        this.textures = textures;
        this.material = material;
        setupMesh();
    }

    public void draw(Shader shader) {
        int diffuseNr = 1;
        int specularNr = 1;
        shader.use();
        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i); // activate proper texture unit before binding
            // retrieve texture number (the N in diffuse_textureN)
            String number = "";
            String name = textures.get(i).getType();
            if (name.equals("texture_diffuse"))
                number = String.valueOf(diffuseNr++);
            else if (name.equals("texture_specular"))
                number = String.valueOf(specularNr++);
            shader.setInt("material." + name + number, i);
            glBindTexture(GL_TEXTURE_2D, textures.get(i).getId());
            shader.setInt("useTexture", 1);
        }
        // draw mesh
        shader.setVec3("material.diffuseColor", material.getDiffuse());
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        shader.setInt("useTexture", 0);
        glBindVertexArray(0);
    }

    private void setupMesh() {
        VerticesLoader verticesLoader = new VerticesLoader(vertices);
        IndicesLoader indicesLoader = new IndicesLoader(indices);
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        EBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, verticesLoader.loadToFloatBuffer(), GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesLoader.loadToIntBuffer(), GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, true, Vertex.NUMBER_OF_FLOATS * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, Vertex.NUMBER_OF_FLOATS * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Vertex.NUMBER_OF_FLOATS * 4, 6 * 4);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }
}
