package org.uav.presentation.entity.outline;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.drone.DroneEntity;
import org.uav.presentation.rendering.Shader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30C.*;
import static org.uav.utils.OpenGLUtils.drawWithDepthFunc;

public class OutlineEntity {

    private final int droneMaskFBO;
    private final int droneMask;
    private final Shader flatShader;
    private final Shader outlineShader;

    public OutlineEntity(int screenWidth, int screenHeight) throws IOException {
        // Drone mask framebuffer initialization
        droneMaskFBO = glGenFramebuffers();
        droneMask = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, droneMask);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, screenWidth, screenHeight,0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
        // Check if it can be RED
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        float[] borderColor = { 0, 0, 0, 1.0f };
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
        glBindFramebuffer(GL_FRAMEBUFFER, droneMaskFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, droneMask, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Flat shader initialization
        var flatVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/flat/flatShader.vert"));
        var flatFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/flat/flatShader.frag"));
        flatShader = new Shader(flatVertexShaderSource, flatFragmentShaderSource);
        flatShader.use();
        flatShader.setVec4("color", new Vector4f(1,0,0, 1));

        // Outline shader initialization
        var outlineVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/ghost/outlineShader.vert"));
        var outlineFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/ghost/outlineShader.frag"));
        outlineShader = new Shader(outlineVertexShaderSource, outlineFragmentShaderSource);
        outlineShader.use();
        outlineShader.setVec4("color", new Vector4f(0, 1, 0, 0.3f));
    }

    public void generateDroneMask(DroneEntity droneEntity, SimulationState simulationState, MemoryStack stack, float time, Matrix4f view, Matrix4f projection) {
        flatShader.use();
        flatShader.setMatrix4f(stack,"view", view);
        flatShader.setMatrix4f(stack,"projection", projection);

        glBindFramebuffer(GL_FRAMEBUFFER, droneMaskFBO);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        drawOutline(droneEntity, simulationState, flatShader, stack, time);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void drawOutline(DroneEntity droneEntity, SimulationState simulationState, Shader shader, MemoryStack stack, float time) {
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, droneMask);
        simulationState.getPlayerDrone().ifPresent(
                drone -> drawWithDepthFunc(
                        () -> droneEntity.draw(stack, shader, time, drone, simulationState.getJoystickStatus()), GL_GREATER
                )
        );
    }

    public void draw(DroneEntity droneEntity, SimulationState simulationState, MemoryStack stack, float time, Matrix4f view, Matrix4f projection) {
        outlineShader.use();
        outlineShader.setMatrix4f(stack,"view", view);
        outlineShader.setMatrix4f(stack,"projection", projection);

        drawOutline(droneEntity, simulationState, outlineShader, stack, time);
    }
}
