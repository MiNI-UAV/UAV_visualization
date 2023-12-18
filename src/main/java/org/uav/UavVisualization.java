package org.uav;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.uav.logic.assets.AssetDownloader;
import org.uav.logic.assets.AvailableControlModes;
import org.uav.logic.audio.AudioManager;
import org.uav.logic.audio.MusicPlayer;
import org.uav.logic.communication.HeartbeatProducer;
import org.uav.logic.config.BindingConfig;
import org.uav.logic.config.Config;
import org.uav.logic.config.DroneParameters;
import org.uav.logic.config.FullScreenMode;
import org.uav.logic.input.bindingsGeneration.BindingsLoop;
import org.uav.logic.input.handler.InputHandler;
import org.uav.logic.messages.MessageBoard;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.logic.state.simulation.SimulationStateProcessor;
import org.uav.presentation.view.LoadingScreen;
import org.uav.presentation.view.OpenGlScene;
import org.uav.utils.FileMapper;
import org.zeromq.ZContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13C.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL32C.GL_PROGRAM_POINT_SIZE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class UavVisualization {

    private long window;
    private long device;
    private long context;
    private OpenGlScene openGlScene;
    private SimulationState simulationState;
    private SimulationStateProcessor simulationStateProcessor;
    private HeartbeatProducer heartbeatProducer;
    private InputHandler inputHandler;
    private Config config;
    private MusicPlayer musicPlayer;
    private AudioManager audioManager;
    private MessageBoard messageBoard;

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
        simulationStateProcessor.updateSimulationState();
        simulationStateProcessor.updateCamera();
        simulationStateProcessor.nextFrame();
        audioManager.update(simulationState);
        musicPlayer.update();
        messageBoard.deprecateMessages();
        if(simulationState.getCurrentlyControlledDrone().isPresent())
            heartbeatProducer.sustainHeartBeat(simulationState.getCurrentlyControlledDrone().get());
        inputHandler.handleInput();
    }

    private void init() throws IOException {
        // Main config
        config = FileMapper.load(Config.class, Paths.get(System.getProperty("user.dir"), "config.yaml"), new YAMLMapper());
        FileMapper.validateNullable(config);
        // OpenGL, OpenAL
        initializeOpenGlEnvironment();
        initializeOpenAlEnvironment();
        // Controller Bindings Generator
        if(config.getBindingsConfig().getGenerateOnStartUp()) {
            new BindingsLoop(window, config).loop();
        }
        // Other configs
        DroneParameters droneParameters = FileMapper.load(DroneParameters.class, Paths.get(System.getProperty("user.dir"), "drones", config.getDroneSettings().getDroneConfig()), new XmlMapper());
        BindingConfig bindingConfig = FileMapper.load(BindingConfig.class, Paths.get(System.getProperty("user.dir"), config.getBindingsConfig().getSource()), new YAMLMapper());
        // Loading screen
        var loadingScreen = new LoadingScreen(window, config);
        loadingScreen.render("Initializing...");
        // Message Board
        messageBoard = new MessageBoard();
        // Music
        musicPlayer = new MusicPlayer(config.getAudioSettings().getMusicVolume());
        musicPlayer.subscribe(messageBoard.produceSubscriber());
        if(config.getMiscSettings().getEnableMusic()) musicPlayer.setDirectory(Paths.get(System.getProperty("user.dir"), config.getMiscSettings().getMusicDirectory()).toString());
        if(config.getMiscSettings().getMusicOnStartup()) musicPlayer.playOrStop();
        // Connection
        simulationState = new SimulationState(window, config, droneParameters);
        var context = new ZContext();
        // Assets
        loadingScreen.render("Checking assets...");
        var assetDownloader = new AssetDownloader(context, config);
        assetDownloader.checkAndUpdateAssets(config, simulationState, loadingScreen);

        heartbeatProducer = new HeartbeatProducer(config);
        AvailableControlModes availableControlModes = FileMapper.load(AvailableControlModes.class, Paths.get(simulationState.getAssetsDirectory(), "data", "available_control_modes.yaml"), new YAMLMapper());
        simulationStateProcessor = new SimulationStateProcessor(context, simulationState, config, availableControlModes, messageBoard);
        audioManager = new AudioManager(simulationState, droneParameters, config);
        audioManager.play();
        inputHandler = new InputHandler(simulationStateProcessor, simulationState, config, bindingConfig, musicPlayer);
        openGlScene = new OpenGlScene(simulationState, config, loadingScreen, droneParameters, messageBoard);
        simulationStateProcessor.saveDroneModelChecksum(config.getDroneSettings().getDroneConfig());
        // Request drone for the player.
        loadingScreen.render("Spawning drone...");
        simulationStateProcessor.openCommunication();
        simulationStateProcessor.requestFirstDrone();
    }

    private void close() {
        simulationStateProcessor.close();
        musicPlayer.close();
        closeOpenGlEnvironment();
        closeOpenAlEnvironment();
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
        glfwWindowHint(GLFW_SAMPLES, 4);

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
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

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
        glEnable(GL_STENCIL_TEST);
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_PROGRAM_POINT_SIZE);
        glEnable(GL_BLEND);
        // https://stackoverflow.com/questions/28079159/opengl-glsl-texture-transparency
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void initializeOpenAlEnvironment() {
        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create OpenAL context.");
        }
        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);
    }

    private void closeOpenGlEnvironment() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void closeOpenAlEnvironment() {
        alcDestroyContext(context);
        alcCloseDevice(device);
    }

    public static void main(String[] args) throws IOException {
        new UavVisualization().run();
    }

}