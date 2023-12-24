package org.uav.presentation.view;

import org.uav.UavVisualization;
import org.uav.logic.config.Config;
import org.uav.presentation.entity.gui.widget.display.DisplayWidget;
import org.uav.presentation.rendering.Shader;

import java.io.IOException;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public class LoadingScreen {
    private final long window;
    private final Shader textShader;
    private final DisplayWidget displayWidget;

    public LoadingScreen(long window, Config config) throws IOException {
        this.window = window;

        var textVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/text/textShader.vert"));
        var textFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/text/textShader.frag"));
        textShader = new Shader(textVertexShaderSource, textFragmentShaderSource);
        textShader.use();
        textShader.setFloat("gammaCorrection", config.getGraphicsSettings().getGammaCorrection());

        displayWidget = new DisplayWidget(textShader, config);
    }

    public void render(String description) {
        displayWidget.update(description);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        displayWidget.draw(null);
        glfwSwapBuffers(window);
        glfwPollEvents();
    }
}
