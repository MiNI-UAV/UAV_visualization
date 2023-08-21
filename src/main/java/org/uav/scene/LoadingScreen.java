package org.uav.scene;

import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.scene.drawable.gui.widget.loading.LoadingScreenWidget;
import org.uav.scene.shader.Shader;

import java.io.IOException;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public class LoadingScreen {
    private final long window;
    private final Shader shader;
    private final LoadingScreenWidget loadingScreenWidget;

    public LoadingScreen(long window, Config config) throws IOException {
        this.window = window;

        String guiVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/guiShader.vert")).getFile();
        String guiFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/guiShader.frag")).getFile();
        shader = new Shader(guiVertexShaderSource, guiFragmentShaderSource);
        shader.use();

        loadingScreenWidget = new LoadingScreenWidget(config, "");
    }

    public void render(String description) {
        loadingScreenWidget.update(description);
        shader.use();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        loadingScreenWidget.draw(shader);
        glfwSwapBuffers(window);
        glfwPollEvents();
    }
}
