package org.uav;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.uav.config.Config;
import org.uav.input.InputHandler;
import org.uav.model.SimulationState;
import org.uav.processor.SimulationStateProcessor;
import org.uav.queue.HeartbeatProducer;
import org.uav.scene.OpenGlScene;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class UavVisualization {

    private long window;
    private OpenGlScene openGlScene;
    private SimulationState simulationState;
    private SimulationStateProcessor simulationStateProcessor;
    private HeartbeatProducer heartbeatProducer;
    private InputHandler inputHandler;
    private Config config;


    public void run() throws IOException, URISyntaxException {
        init();
        loop();
        close();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            update();
            openGlScene.render();
        }
    }

    public void update() {
        heartbeatProducer.sustainHeartBeat(simulationState.getCurrentlyControlledDrone());
        inputHandler.handleInput(simulationState.getWindow());
        simulationState.getCamera().updateCamera();
        simulationStateProcessor.updateCurrentEntityStatuses();
    }

    private void init() throws IOException, URISyntaxException {
        config = Config.loadConfig("config.yaml");
        heartbeatProducer = new HeartbeatProducer(config);
        initializeOpenGlEnvironment();
        simulationState = new SimulationState(config, window);
        initSimulationStateProcessor(simulationState);
        inputHandler = new InputHandler(simulationStateProcessor, simulationState, config);
        openGlScene = new OpenGlScene(simulationState, config);
    }

    private void initSimulationStateProcessor(SimulationState simulationState) {
        simulationStateProcessor = new SimulationStateProcessor(simulationState, config);
        simulationStateProcessor.openCommunication();
        simulationStateProcessor.requestNewDrone();
    }

    private void close() {
        simulationStateProcessor.close();
        closeOpenGlEnvironment();
    }

    private void initializeOpenGlEnvironment() {
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW.
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(config.windowWidth, config.windowHeight, "UAV visualization", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Set up a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);


        // Make the window visible
        glfwShowWindow(window);
    }

    private void closeOpenGlEnvironment() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        new UavVisualization().run();
    }

}