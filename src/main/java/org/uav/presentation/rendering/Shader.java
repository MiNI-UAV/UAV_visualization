package org.uav.presentation.rendering;

import com.google.common.primitives.Floats;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32C.GL_GEOMETRY_SHADER;

public class Shader {

    public int shaderProgram;

    public Shader(InputStream vertexShaderStream, InputStream fragmentShaderStream) throws IOException {
        // Read GLSL files
        String vertexShaderSource = IOUtils.toString(vertexShaderStream, StandardCharsets.UTF_8);
        String fragmentShaderSource = IOUtils.toString(fragmentShaderStream, StandardCharsets.UTF_8);

        IntBuffer success = BufferUtils.createIntBuffer(1);
        int vertexShader = getVertexShader(vertexShaderSource, success);
        int fragmentShader = getFragmentShader(fragmentShaderSource, success);

        // Set Up Shader Program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        glGetShaderiv(shaderProgram, GL_LINK_STATUS, success);
        if(success.get() == 0)
            System.err.println("Failed to set up shader program");

        // Clean up
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public Shader(InputStream vertexShaderStream, InputStream geometryShaderStream, InputStream fragmentShaderStream) throws IOException {
        // Read GLSL files
        String vertexShaderSource = IOUtils.toString(vertexShaderStream, StandardCharsets.UTF_8);
        String geometryShaderSource = IOUtils.toString(geometryShaderStream, StandardCharsets.UTF_8);
        String fragmentShaderSource = IOUtils.toString(fragmentShaderStream, StandardCharsets.UTF_8);

        IntBuffer success = BufferUtils.createIntBuffer(1);
        int vertexShader = getVertexShader(vertexShaderSource, success);
        int geometryShader = getFragmentShader(fragmentShaderSource, success);
        int fragmentShader = getGeometryShader(geometryShaderSource, success);

        // Set Up Shader Program
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, geometryShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        glGetShaderiv(shaderProgram, GL_LINK_STATUS, success);
        if(success.get() == 0)
            System.err.println("Failed to set up shader program");

        // Clean up
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private static int getGeometryShader(String geometryShaderSource, IntBuffer success) {
        // Set Up Fragment Shader
        int geometryShader;
        geometryShader = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geometryShader, geometryShaderSource);
        glCompileShader(geometryShader);
        glGetShaderiv(geometryShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
            System.err.println("Failed to set up geometry shader");
        success.rewind();
        return geometryShader;
    }

    private static int getFragmentShader(String fragmentShaderSource, IntBuffer success) {
        // Set Up Fragment Shader
        int fragmentShader;
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        glGetShaderiv(fragmentShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
            System.err.println("Failed to set up fragment shader");
        success.rewind();
        return fragmentShader;
    }

    private static int getVertexShader(String vertexShaderSource, IntBuffer success) {
        // Set Up Vertex Shader
        int vertexShader;
        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
            System.err.println("Failed to set up vertex shader");
        success.rewind();
        return vertexShader;
    }

    public void use() {
        glUseProgram(shaderProgram);
    }

    public void setBool(String name, Boolean value) {
        glUniform1i(glGetUniformLocation(shaderProgram, name), value.compareTo(false));
    }

    public void setInt(String name, int value) {
        glUniform1i(glGetUniformLocation(shaderProgram, name), value);
    }

    public void setFloat(String name, float value) {
        glUniform1f(glGetUniformLocation(shaderProgram, name), value);

    }
    public void setVec4(String name, Vector4f vec) {
        glUniform4f(glGetUniformLocation(shaderProgram, name), vec.x, vec.y, vec.z, vec.w);
    }

    public void setVec3(String name, Vector3f vec) {
        glUniform3f(glGetUniformLocation(shaderProgram, name), vec.x, vec.y, vec.z);
    }
    public void setMatrix4f(MemoryStack stack, String name, Matrix4f matrix) {
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, name), false, matrix.get(stack.mallocFloat(16)));
    }

    public void setVec3Array(MemoryStack stack, String name, List<Vector3f> value) {
        float[] array = Floats.toArray(value.stream().flatMap(v -> Stream.of(v.x, v.y, v.z)).toList());
        FloatBuffer buffer = stack.mallocFloat(value.size() * 3);
        buffer.put(array);
        buffer.rewind();
        glUniform3fv(glGetUniformLocation(shaderProgram, name), buffer);
    }

}
