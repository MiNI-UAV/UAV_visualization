package org.opengl.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;

public class Shader {

    public int shaderProgram;

    public Shader(String vertexShaderPath, String fragmentShaderPath) throws IOException {
        // Read GLSL files
        String vertexShaderSource = Files.readString(new File(vertexShaderPath).toPath());
        String fragmentShaderSource = Files.readString(new File(fragmentShaderPath).toPath());

        IntBuffer success = BufferUtils.createIntBuffer(1);
        // Set Up Vertex Shader
        int vertexShader;
        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
            System.err.println("Failed to set up vertex shader");
        success.rewind();

        // Set Up Fragment Shader
        int fragmentShader;
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        glGetShaderiv(vertexShader, GL_COMPILE_STATUS, success);
        if(success.get() == 0)
            System.err.println("Failed to set up fragment shader");
        success.rewind();

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
    public void setVec4(String name, float v1, float v2, float v3, float v4) {
        glUniform4f(glGetUniformLocation(shaderProgram, name), v1, v2, v3, v4);
    }

    public void setVec3(String name, Vector3f vec) {
        glUniform3f(glGetUniformLocation(shaderProgram, name), vec.x, vec.y, vec.z);
    }
    public void setMatrix4f(MemoryStack stack, String name, Matrix4f matrix) {
        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, name), false, matrix.get(stack.mallocFloat(16)));
    }

}
