package org.uav;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.config.FullScreenMode;
import org.uav.input.InputHandler;
import org.uav.model.SimulationState;
import org.uav.processor.SimulationStateProcessor;
import org.uav.queue.HeartbeatProducer;
import org.uav.scene.LoadingScreen;
import org.uav.scene.OpenGlScene;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
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
    private DroneParameters droneParameters;

    public void run() throws IOException {
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
        simulationStateProcessor.updateCurrentEntityStatuses();
        simulationState.getCamera().updateCamera();
    }

    private void init() throws IOException {
        config = Config.load(Paths.get("config.yaml"));
        droneParameters = DroneParameters.load(Paths.get("drones",config.getDroneSettings().getDroneModel() + ".xml"));
        initializeOpenGlEnvironment();
        var loadingScreen = new LoadingScreen(window, config);
        loadingScreen.render("Initializing...");
        heartbeatProducer = new HeartbeatProducer(config);
        simulationState = new SimulationState(config, window);
        simulationStateProcessor = new SimulationStateProcessor(simulationState, config);
        loadingScreen.render("Checking assets...");
        simulationStateProcessor.checkAndUpdateAssets(config, simulationState, loadingScreen);
        inputHandler = new InputHandler(simulationStateProcessor, simulationState, config);
        openGlScene = new OpenGlScene(simulationState, config, loadingScreen, droneParameters);
        simulationStateProcessor.openCommunication();
        simulationStateProcessor.saveDroneModelChecksum(config.getDroneSettings().getDroneModel());
        loadingScreen.render("Spawning drone...");
        simulationStateProcessor.requestNewDrone();

//        String musicFilePath = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("assets/audio/music.wav")).getFile();
//        try {
//            musicPlayer = new MusicPlayer(musicFilePath);
//            musicPlayer.play();
//        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
//            throw new RuntimeException(e);
//        }
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
        if(config.getGraphicsSettings().getFullScreenMode() == FullScreenMode.Borderless) glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        long fullScreen = NULL;
        if(config.getGraphicsSettings().getFullScreenMode() == FullScreenMode.FullScreen) {
            var monitors = glfwGetMonitors();
            var monitor_no = config.getGraphicsSettings().getMonitor();
            if(monitor_no == null)
            {
                fullScreen = monitors.get();
            }
            else
            {
                try
                {
                    fullScreen = monitors.get(monitor_no.intValue());
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Monitor out of the range, using primary");
                }
            }
        }

        window = glfwCreateWindow(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight(), "UAV visualization", fullScreen, NULL);
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

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        // https://stackoverflow.com/questions/28079159/opengl-glsl-texture-transparency
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void closeOpenGlEnvironment() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static void main(String[] args) throws IOException {
        new UavVisualization().run();
    }

}