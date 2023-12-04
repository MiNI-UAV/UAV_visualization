package org.uav.scene;

import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.scene.gui.widget.display.DisplayWidget;
import org.uav.scene.shader.Shader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public class BindingsScreen {
    private final long window;
    private final Shader shader;
    private final DisplayWidget displayWidget;

    public BindingsScreen(long window, Config config) throws IOException {
        this.window = window;

        var guiVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/gui/guiShader.vert"));
        var guiFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/gui/guiShader.frag"));
        shader = new Shader(guiVertexShaderSource, guiFragmentShaderSource);
        shader.use();
        shader.setBool("useGammaCorrection", config.getGraphicsSettings().getUseGammaCorrection());
        shader.setFloat("gammaCorrection", config.getGraphicsSettings().getGammaCorrection());

        displayWidget = new DisplayWidget(config, new ArrayList<>());
    }

    public void render(List<String> description) {
        displayWidget.update(description);
        shader.use();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        displayWidget.draw(shader);
        glfwSwapBuffers(window);
        glfwPollEvents();
    }
}
