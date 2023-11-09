package org.uav.model;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.uav.importer.IndicesLoader;
import org.uav.importer.VerticesLoader;
import org.uav.scene.shader.Shader;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {
    private final List<ModelVertex> vertices;
    private final List<Integer> indices;
    private final List<Texture> textures;
    private final boolean transparentTexture;
    private final Material material;
    private int VAO;

    public Mesh(List<ModelVertex> vertices, List<Integer> indices, List<Texture> textures, boolean transparentTexture, Material material) {
        this.vertices = vertices;
        this.indices = indices;
        this.textures = textures;
        this.transparentTexture = transparentTexture;
        this.material = material;
        setupMesh();
    }

    public void draw(MemoryStack stack, Shader shader, Matrix4f modelMatrix) {
        int diffuseNr = 1;
        int specularNr = 1;
        shader.use();
        for (int i = 0; i < 1/*textures.size()*/; i++) { // TODO Remove. We don't need multiple texture samplers for models for now.
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

        // TODO TEXCOORD_1 2 3 4 ...
        // draw mesh
        shader.setMatrix4f(stack,"model", modelMatrix);
        shader.setVec3("material.ambient", material.getDiffuse());
        shader.setVec3("material.diffuse", material.getDiffuse());
        shader.setVec3("material.specular", material.getDiffuse());
        shader.setFloat("material.shininess", 16.0f);
        shader.setFloat("material.roughness", material.roughness);
        shader.setFloat("material.metallic", material.metallic);
        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        shader.setInt("useTexture", 0);
        glBindVertexArray(0);
    }

    private void setupMesh() {
        IndicesLoader indicesLoader = new IndicesLoader(indices);
        VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, VerticesLoader.loadToFloatBuffer(vertices), GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesLoader.loadToIntBuffer(), GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, true, ModelVertex.NUMBER_OF_FLOATS * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, ModelVertex.NUMBER_OF_FLOATS * 4, 3 * 4);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, ModelVertex.NUMBER_OF_FLOATS * 4, 6 * 4);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

    public boolean isTransparent() {
        return transparentTexture;
    }
}
