package org.uav.presentation.model;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;
import org.uav.presentation.model.importer.IndicesLoader;
import org.uav.presentation.model.importer.VerticesLoader;
import org.uav.presentation.rendering.Shader;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh implements AutoCloseable {
    private final List<ModelVertex> vertices;
    private final List<Integer> indices;
    @Nullable
    private final Texture albedoTexture;
    @Nullable
    private final Texture normalTexture;
    @Nullable
    private final Texture metallicRoughnessTexture;
    @Nullable
    private final Texture ambientOcclusionTexture;
    private final List<Texture> textures;
    private final boolean isTransparentTexture;
    private final Material material;
    private int VAO;
    private final FloatBuffer modelMatrixBuffer;

    public Mesh(
            List<ModelVertex> vertices,
            List<Integer> indices,
            List<Texture> textures,
            @Nullable Texture albedoTexture,
            @Nullable Texture normalTexture,
            @Nullable Texture metallicRoughnessTexture,
            @Nullable Texture ambientOcclusionTexture,
            boolean isTransparentTexture,
            Material material
    ) {
        this.vertices = vertices;
        this.indices = indices;
        this.albedoTexture = albedoTexture;
        this.normalTexture = normalTexture;
        this.metallicRoughnessTexture = metallicRoughnessTexture;
        this.ambientOcclusionTexture = ambientOcclusionTexture;
        this.textures = textures;
        this.isTransparentTexture = isTransparentTexture;
        this.material = material;
        modelMatrixBuffer = MemoryUtil.memCallocFloat(16);
        setupMesh();
    }

    public void draw(Shader shader, Matrix4f modelMatrix) {
        shader.use();

        bindTextures(shader, albedoTexture, "useAlbedoMap", "albedoMap", 0);
        bindTextures(shader, normalTexture, "useNormalMap", "normalMap", 1);
        bindTextures(shader, metallicRoughnessTexture, "useMetallicRoughnessMap", "metallicRoughnessMap", 2);
        bindTextures(shader, ambientOcclusionTexture, "useAmbientOcclusionMap", "ambientOcclusionMap", 3);

        // TODO TEXCOORD_1 2 3 4 ...
        // draw mesh
        modelMatrix.get(modelMatrixBuffer);
        shader.setMatrix4f("model", modelMatrixBuffer);
        shader.setVec4("material.albedo", material.getAlbedo());
        shader.setFloat("material.normalScale", material.getNormalScale());
        shader.setFloat("material.roughness", material.getRoughness());
        shader.setFloat("material.metallic", material.getMetallic());
        shader.setFloat("material.aoStrength", material.getAoStrength());
        glBindVertexArray(VAO);
        if (indices.isEmpty())
            glDrawArrays(GL_TRIANGLES, 0, vertices.size());
        else
            glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    private void bindTextures(Shader shader, Texture texture, String useVariable, String textureName, int index) {
        if(texture != null) {
            shader.setInt(textureName, index);
            glActiveTexture(GL_TEXTURE0 + index);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
            shader.setBool(useVariable, true);
        } else
            shader.setBool(useVariable, false);
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
        return isTransparentTexture;
    }

    @Override
    public void close() throws Exception {
        MemoryUtil.memFree(modelMatrixBuffer);
    }
}
