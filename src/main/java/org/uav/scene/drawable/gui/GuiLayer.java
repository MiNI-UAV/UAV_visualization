package org.uav.scene.drawable.gui;

import org.joml.Vector2i;
import org.uav.importer.IndicesLoader;
import org.uav.importer.VerticesLoader;
import org.uav.model.Texture;
import org.uav.model.Vertex;
import org.uav.scene.shader.Shader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.nio.ByteBuffer.allocateDirect;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class GuiLayer {
    private Texture texture;
    private final int VAO;
    private final List<Integer> indices;
    private int format;
    private final DrawableGuiLayer guiLayer;
    private ByteBuffer imageDirectByteBuffer;
    private final boolean  dynamicTexture;
    private Vector2i canvasSize;

    public GuiLayer(List<Vertex> vertices, BufferedImage texture) {
        indices = List.of(
                1, 0, 3,   // first triangle
                1, 2, 3    // second triangle
        );
        VAO = loadPrimitives(vertices);
        guiLayer = (Graphics2D g) -> {};
        dynamicTexture = false;
        loadTexture(texture);
    }

    public GuiLayer(List<Vertex> vertices, int width, int height, DrawableGuiLayer guiLayer) {
        indices = List.of(
                1, 0, 3,   // first triangle
                1, 2, 3    // second triangle
        );
        VAO = loadPrimitives(vertices);
        dynamicTexture = true;
        this.guiLayer = guiLayer;
        createEmptyTexture(width, height);
    }

    private void createEmptyTexture(int width, int height) {
        setupTexture(new BufferedImage(width, height, TYPE_INT_ARGB));
    }

    private void loadTexture(BufferedImage img) {
        setupTexture(img);
    }

    private void setupTexture(BufferedImage img) {
        canvasSize = new Vector2i(img.getWidth(), img.getHeight());
        imageDirectByteBuffer = allocateDirect(img.getHeight() * img.getWidth() * img.getColorModel().getNumComponents());
        imageDirectByteBuffer.put(ByteBuffer.wrap(extractImageData(img)));
        imageDirectByteBuffer.position(0);

        int components = img.getColorModel().getNumComponents();
        format = switch(components) {
            case 1 -> GL_BACK;
            case 4 -> GL_RGBA;
            default -> GL_RGB;
        };

        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, format, img.getWidth(), img.getHeight(), 0, format, GL_UNSIGNED_BYTE, imageDirectByteBuffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        texture = new Texture(textureId, "texture_diffuse");
    }

    private int loadPrimitives(List<Vertex> vertices) {
        final int VAO;
        VerticesLoader verticesLoader = new VerticesLoader(vertices);
        IndicesLoader indicesLoader = new IndicesLoader(indices);
        VAO = glGenVertexArrays();
        int VBO = glGenBuffers();
        int EBO = glGenBuffers();
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
        return VAO;
    }

    private byte[] extractImageData(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Get the color model of the BufferedImage
        int numComponents = image.getColorModel().getNumComponents();
        boolean hasAlpha = image.getColorModel().hasAlpha();

        // Determine the byte size per pixel based on the color model
        int bytesPerPixel = numComponents;

        // Create a byte array to hold the image data
        byte[] imageData = new byte[width * height * bytesPerPixel];

        // Iterate through each pixel and store the pixel data in the byte array
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);

                // Extract individual color components (RGBA)
                int red = (pixel) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = (pixel >> 16) & 0xFF;
                int alpha = hasAlpha ? (pixel >> 24) & 0xFF : 255;

                // Calculate the starting index in the byte array for the current pixel
                int index = (y * width + x) * bytesPerPixel;

                // Store the color components in the byte array
                imageData[index] = (byte) blue;
                imageData[index + 1] = (byte) green;
                imageData[index + 2] = (byte) red;
                if (hasAlpha) {
                    imageData[index + 3] = (byte) alpha;
                }
            }
        }
        return imageData;
    }

    public void draw(Shader shader) {
        shader.use();

        if(dynamicTexture) {
            var overlayImg = new BufferedImage(canvasSize.x, canvasSize.y, TYPE_INT_ARGB);
            guiLayer.draw((Graphics2D) overlayImg.getGraphics());
            imageDirectByteBuffer.put(ByteBuffer.wrap(extractImageData(overlayImg)));
            imageDirectByteBuffer.position(0);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
            glTexImage2D(GL_TEXTURE_2D, 0, format, canvasSize.x, canvasSize.y, 0, format, GL_UNSIGNED_BYTE, imageDirectByteBuffer);
        }


        glActiveTexture(GL_TEXTURE0);
        String name = texture.getType();
        shader.setInt("material." + name + 1, 0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        shader.setInt("useTexture", 1);

        glBindVertexArray(VAO);
        glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
        shader.setInt("useTexture", 0);
        glBindVertexArray(0);
    }

}
