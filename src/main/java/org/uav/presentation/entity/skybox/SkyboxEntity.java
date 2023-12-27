package org.uav.presentation.entity.skybox;

import org.uav.UavVisualization;
import org.uav.presentation.model.importer.VerticesLoader;
import org.uav.presentation.rendering.Shader;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.uav.utils.OpenGLUtils.getImageInfo;

public class SkyboxEntity {
    private final Shader skyboxShader;
    private final int VAO;
    private int textureID;

    public SkyboxEntity(String assetsDirectory) throws IOException {
        var skyboxVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/skybox/skyboxShader.vert"));
        var skyboxFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/skybox/skyboxShader.frag"));
        skyboxShader = new Shader(skyboxVertexShaderSource, skyboxFragmentShaderSource);
        skyboxShader.use();
        skyboxShader.setInt("skybox", 0);

        var vertices = new ArrayList<SkyboxVertex>() {{
                    add(new SkyboxVertex(-1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f,  1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f,  1.0f));
                    add(new SkyboxVertex(-1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f,  1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f,  1.0f));
                    add(new SkyboxVertex(-1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(-1.0f,  1.0f,  1.0f));
                    add(new SkyboxVertex(-1.0f,  1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f, -1.0f));
                    add(new SkyboxVertex(-1.0f, -1.0f,  1.0f));
                    add(new SkyboxVertex(1.0f, -1.0f,  1.0f));
        }};
        VAO = loadPrimitives(vertices);
        loadCubemap(assetsDirectory);
    }

    public void draw(FloatBuffer viewNoTranslationBuffer, FloatBuffer projectionBuffer) {
        glDepthFunc(GL_LEQUAL);

        skyboxShader.use();
        skyboxShader.setMatrix4f("view", viewNoTranslationBuffer);
        skyboxShader.setMatrix4f("projection", projectionBuffer);

        glBindVertexArray(VAO);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);
        glDepthFunc(GL_LESS);
    }

    private void loadCubemap(String assetsDirectory) throws IOException {
        var skyboxDir = Paths.get(assetsDirectory, "core", "skybox").toString();

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);

        var sides = List.of(
                "skybox_front.bmp",
                "skybox_back.bmp",
                "skybox_right.bmp",
                "skybox_left.bmp",
                "skybox_top.bmp",
                "skybox_bottom.bmp"
        );

        for(int i = 0; i < sides.size(); i++) {
            var path = Paths.get(skyboxDir, sides.get(i));
            var img = ImageIO.read(new File(path.toString()));
            var imageInfo = getImageInfo(img);
            glTexImage2D(
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    0,
                    imageInfo.internalFormat(),
                    img.getWidth(),
                    img.getHeight(),
                    0,
                    imageInfo.format(),
                    GL_UNSIGNED_BYTE,
                    imageInfo.imageDirectByteBuffer()
            );
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
    }

    private int loadPrimitives(List<SkyboxVertex> vertices) {
        final int VAO;
        VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, VerticesLoader.loadToFloatBuffer(vertices), GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, true, SkyboxVertex.NUMBER_OF_FLOATS * 4, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
        return VAO;
    }
}
