package org.uav.presentation.entity.text;

import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.config.Config;
import org.uav.presentation.entity.sprite.SpriteVertex;
import org.uav.presentation.model.importer.IndicesLoader;
import org.uav.presentation.model.importer.VerticesLoader;
import org.uav.presentation.rendering.Shader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.uav.utils.IOUtils.ioResourceToByteBuffer;

public class TextEngine implements AutoCloseable {

    int VAO;
    int VBO;
    List<Integer> indices;
    Shader textShader;
    private final int bitmapW;
    private final int bitmapH;
    private final Vector2f screen;
    private final Vector2f screenRatio;
    private final int fontHeight;
    private final float fontHeightNorm;
    private final STBTTBakedChar.Buffer cdata;
    private final ByteBuffer ttf;

    private final STBTTFontinfo info;

    private final int ascent;
    private final int descent;
    private final int lineGap;
    private int texID;
    @Setter
    private Vector4f color;
    private Vector2f position;
    @Setter
    private Vector4f cropRectangle;

    public TextEngine(Vector4f widgetPosition, float fontHeightNorm, Shader textShader, Config config) {
        this.position = new Vector2f();
        screen = new Vector2f(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight());
        this.fontHeight = (int) (fontHeightNorm * screen.y);
        screenRatio = new Vector2f(((widgetPosition.w - widgetPosition.z) / 2), ((widgetPosition.x - widgetPosition.y) / 2));
        this.fontHeightNorm = fontHeightNorm / screenRatio.y;
        color = new Vector4f(1,1,1,1);
        this.bitmapW = fontHeight * 10; // At least 10 characters.
        this.bitmapH = fontHeight * 10; // At least 10 rows.
        this.textShader = textShader;
        cropRectangle = new Vector4f(-1,-1,2,2);

        try {
            ttf = ioResourceToByteBuffer("FreeSans.ttf", 512 * 1024);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        info = STBTTFontinfo.create();
        if (!stbtt_InitFont(info, ttf)) {
            throw new IllegalStateException("Failed to initialize font information.");
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer pAscent  = stack.mallocInt(1);
            IntBuffer pDescent = stack.mallocInt(1);
            IntBuffer pLineGap = stack.mallocInt(1);

            stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);

            ascent = pAscent.get(0);
            descent = pDescent.get(0);
            lineGap = pLineGap.get(0);
        }

        cdata = init();

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
        IndicesLoader indicesLoader = new IndicesLoader(indices);
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();
        int EBO = glGenBuffers();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, VerticesLoader.loadToFloatBuffer(vertices), GL_DYNAMIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesLoader.loadToIntBuffer(), GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, true, SpriteVertex.NUMBER_OF_FLOATS * 4, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, SpriteVertex.NUMBER_OF_FLOATS * 4, 2 * 4);
        glEnableVertexAttribArray(1);
        glBindBuffer( GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void renderText(String text) {
        float scale = stbtt_ScaleForPixelHeight(info, fontHeight);
        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint = stack.mallocInt(1);

            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            int lineStart = 0;

            float factorX = 1.0f;// / getContentScaleX();
            float factorY = 1.0f;// / getContentScaleY();

            float lineY = 0.0f;

            for (int i = 0, to = text.length(); i < to; ) {
                i += getCP(text, to, i, pCodePoint);

                int cp = pCodePoint.get(0);
                if(cp == '\n') {
                    y.put(0, lineY = y.get(0) + (float) (ascent - descent + lineGap ) * scale);
                    x.put(0, 0.0f);
                }
                float cpX = x.get(0);
                stbtt_GetBakedQuad(cdata, bitmapW, bitmapH, cp - 32, x, y, q, true);
                x.put(0, scale(cpX, x.get(0), factorX));
                if (i < to) {
                    getCP(text, to, i, pCodePoint);
                    x.put(0, x.get(0) + stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0)) * scale);
                }

                float
                        x0 = scale(cpX, q.x0(), factorX) / screen.x / screenRatio.x,
                        x1 = scale(cpX, q.x1(), factorX) / screen.x / screenRatio.x,
                        y0 = scale(lineY, q.y0(), factorY) / screen.y / screenRatio.y,
                        y1 = scale(lineY, q.y1(), factorY) / screen.y / screenRatio.y;

                var vertices = List.of(
                        new SpriteVertex(new Vector2f(position.x + x0, position.y - y0), new Vector2f(q.s0(), q.t0())),
                        new SpriteVertex(new Vector2f(position.x + x1, position.y - y0), new Vector2f(q.s1(), q.t0())),
                        new SpriteVertex(new Vector2f(position.x + x1, position.y - y1), new Vector2f(q.s1(), q.t1())),
                        new SpriteVertex(new Vector2f(position.x + x0, position.y - y1), new Vector2f(q.s0(), q.t1()))
                );
                textShader.use();
                textShader.setVec4("color", color);
                textShader.setVec4("cropRectangle", cropRectangle);
                glBindVertexArray(VAO);
                glBindTexture(GL_TEXTURE_2D, texID);
                glBindBuffer(GL_ARRAY_BUFFER, VBO);
                glBufferSubData(GL_ARRAY_BUFFER, 0, VerticesLoader.loadToFloatBuffer(vertices));
                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glDrawElements(GL_TRIANGLES, indices.size(), GL_UNSIGNED_INT, 0);
                glBindVertexArray(0);
            }
        }
    }

    public void setPosition(float x, float y) {
        position = new Vector2f(x, y);
    }

    private static float scale(float center, float offset, float factor) {
        return (offset - center) * factor + center;
    }

    private STBTTBakedChar.Buffer init() {
        texID = glGenTextures();
        var cdata = STBTTBakedChar.malloc(96);

        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitmapW * bitmapH);
        var res = stbtt_BakeFontBitmap(ttf, fontHeight, bitmap, bitmapW, bitmapH, 32, cdata);
        if(res < 0) throw new IllegalArgumentException("BakeFontBitmap result: " + res + ". Not enough space for characters to fit!");

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glBindTexture(GL_TEXTURE_2D, texID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, bitmapW, bitmapH, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        return cdata;
    }

    public float getFontHeight() {
        return fontHeightNorm;
    }
    public float getStringWidth(String text) {
        return getStringWidth(text, 0, text.length());
    }

    public float getStringWidth(String text, int from, int to) {
        int width = 0;

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint       = stack.mallocInt(1);
            IntBuffer pAdvancedWidth   = stack.mallocInt(1);
            IntBuffer pLeftSideBearing = stack.mallocInt(1);

            int i = from;
            while (i < to) {
                i += getCP(text, to, i, pCodePoint);
                int cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(info, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);

                if (i < to) {
                    getCP(text, to, i, pCodePoint);
                    width += stbtt_GetCodepointKernAdvance(info, cp, pCodePoint.get(0));
                }
            }
        }

        return width * stbtt_ScaleForPixelHeight(info, fontHeight) / screen.x / screenRatio.x;
    }

    private static int getCP(String text, int to, int i, IntBuffer cpOut) {
        char c1 = text.charAt(i);
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            char c2 = text.charAt(i + 1);
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2));
                return 2;
            }
        }
        cpOut.put(0, c1);
        return 1;
    }

    @Override
    public void close() throws Exception {
        cdata.free();
    }
}
