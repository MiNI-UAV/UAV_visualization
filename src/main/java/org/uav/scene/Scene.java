package org.uav.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.uav.OpenGLScene;
import org.uav.camera.Camera;
import org.uav.config.Configuration;
import org.uav.input.InputHandler;
import org.uav.input.JoystickButtonFunctions;
import org.uav.status.DroneStatus;
import org.uav.model.Model;
import org.uav.queue.PositionConsumer;
import org.uav.queue.PropellerConsumer;
import org.uav.shader.Shader;
import org.uav.utils.Convert;
import org.zeromq.ZContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.uav.importer.GltfImporter.loadModel;

public class Scene implements AutoCloseable {
    private final long window;
    private final int windowHeight;
    private final int windowWidth;
    private final Camera camera;
    private final Configuration configuration;
    private final InputHandler inputHandler;

    private final Vector3f DAY_COLOR = new Vector3f(0.529f, 0.808f, 0.922f);
    private final Vector3f NIGHT_COLOR = new Vector3f(0f, 0f, 0f);
    private float dayFactor = 1.0f;
    private final DroneStatus droneStatus;
    private Model droneModel, environmentModel, busterModel, axisModel;
    Shader lightSourceShader;
    private final PositionConsumer positionConsumer;
    private final PropellerConsumer propellerConsumer;

    // camera movement

    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastTime = 0.0f; // Time of last frame


    public Scene(long window, int windowWidth, int windowHeight) throws IOException, URISyntaxException {
        this.window = window;
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        ZContext context = new ZContext();
        droneStatus = new DroneStatus();
        positionConsumer = new PositionConsumer(context, droneStatus);
        propellerConsumer = new PropellerConsumer(context, droneStatus);
        positionConsumer.start();
        propellerConsumer.start();

        camera = new Camera();
        configuration = new Configuration();
        inputHandler = new InputHandler(configuration, context);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(DAY_COLOR.x, DAY_COLOR.y, DAY_COLOR.z, 0.0f);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        setUpDrawables();
        setUpShaders();
        setUpJoystick();
    }

    private void setUpJoystick() {
        var joystickMapping = new HashMap<Integer, Integer>();
        //HOTAS
        // joystickMapping.put(5, 0);
        // joystickMapping.put(2, 1);
        // joystickMapping.put(0, 2);
        // joystickMapping.put(1, 3);
        //XBOX controller
        joystickMapping.put(0, 0);
        joystickMapping.put(1, 1);
        joystickMapping.put(2, 2);
        joystickMapping.put(3, 3);

        var joystickInversionMapping = new HashMap<Integer, Boolean>();
        //HOTAS
        // joystickInversionMapping.put(5, false);
        // joystickInversionMapping.put(2, true);
        // joystickInversionMapping.put(0, false);
        // joystickInversionMapping.put(1, true);
        //XBOX controller
        joystickInversionMapping.put(0, false);
        joystickInversionMapping.put(1, true);
        joystickInversionMapping.put(2, false);
        joystickInversionMapping.put(3, true);

        var joystickButtonMapping = new HashMap<Integer, JoystickButtonFunctions>();
        //XBOX controller
        joystickButtonMapping.put(0,JoystickButtonFunctions.nextCamera);
        joystickButtonMapping.put(1,JoystickButtonFunctions.prevCamera);
        joystickButtonMapping.put(6,JoystickButtonFunctions.angleMode);
        joystickButtonMapping.put(7,JoystickButtonFunctions.acroMode);

        configuration.joystickMapping = joystickMapping;
        configuration.joystickInversionMapping = joystickInversionMapping;
        configuration.joystickButtonsMapping = joystickButtonMapping;
    }

    private void setUpShaders() throws IOException {

        String phongVertexShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/phongShader.vert")).getFile();
        String phongFragmentShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/phongShader.frag")).getFile();
        String gouraudVertexShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/gouraudShader.vert")).getFile();
        String gouraudFragmentShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/gouraudShader.frag")).getFile();
        String flatVertexShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/flatShader.vert")).getFile();
        String flatFragmentShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/flatShader.frag")).getFile();
        String lightSourceVertexShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/lightSourceShader.vert")).getFile();
        String lightSourceFragmentShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/lightSourceShader.frag")).getFile();

        configuration.phongShader = new Shader(phongVertexShaderSource, phongFragmentShaderSource);
        configuration.gouraudShader = new Shader(gouraudVertexShaderSource, gouraudFragmentShaderSource);
        configuration.flatShader =new Shader(flatVertexShaderSource, flatFragmentShaderSource);
        configuration.shader = configuration.phongShader;
        lightSourceShader = new Shader(lightSourceVertexShaderSource, lightSourceFragmentShaderSource);

        configuration.phongShader.use();
        configuration.phongShader.setVec3("backgroundColor", DAY_COLOR);
        setUpFog(configuration.phongShader);
        setUpLights(configuration.phongShader);

        configuration.gouraudShader.use();
        configuration.gouraudShader.setVec3("backgroundColor", DAY_COLOR);
        setUpFog(configuration.gouraudShader);
        setUpLights(configuration.gouraudShader);

        configuration.flatShader.use();
        configuration.flatShader.setVec3("backgroundColor", DAY_COLOR);
        setUpFog(configuration.flatShader);
        setUpLights(configuration.flatShader);

        lightSourceShader.use();
        lightSourceShader.setVec3("lightColor", new Vector3f(1.f, 	1.f, 1.f));
        setUpFog(lightSourceShader);
    }

    private void setUpDrawables() throws URISyntaxException, IOException {

        Supplier<Quaternionf> clockwiseRotation =
                () -> new Quaternionf(0,0,1 * sin(glfwGetTime()*1000),-cos(glfwGetTime()*1000)).normalize();
        Supplier<Quaternionf> counterClockwiseRotation =
                () -> new Quaternionf(0,0,1 * sin(glfwGetTime()*1000),cos(glfwGetTime()*1000)).normalize();
        droneModel = loadModel("models/drone.gltf", "textures/drone");
        droneModel.setAnimation(null, clockwiseRotation, null, List.of("propeller.2", "propeller.3"));
        droneModel.setAnimation(null, counterClockwiseRotation, null, List.of("propeller.1", "propeller.4"));
        //busterModel = loadModel("models/buster.gltf", "textures/buster");
        //busterModel.setAnimation(null, clockwiseRotation, null, List.of("Drone_Turb_Blade_L_body_0"));
        //busterModel.setAnimation(null, counterClockwiseRotation, null, List.of("Drone_Turb_Blade_R_body_0"));
        environmentModel = loadModel("models/dust.gltf", "textures/dust");
        axisModel = loadModel("models/axis.gltf", "textures/axis");
        //environmentModel = loadModel("models/field.gltf", "textures/field");
    }


    public void loop() {

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            toggleDayNight(configuration.shader, lightSourceShader);
            changeFog(configuration.shader, lightSourceShader);
            changeCamera();

            Matrix4f view = camera.getViewMatrix();
            Matrix4f projection = new Matrix4f().perspective(toRadians(camera.getFov()), (float) windowWidth / windowHeight, 0.1f, 1000f);

            inputHandler.processInput(window);

            // Drawing
            try (MemoryStack stack = MemoryStack.stackPush()) {
                configuration.shader.setVec3("viewPos", camera.getCameraPos());
                configuration.shader.setMatrix4f(stack,"view", view);
                configuration.shader.setMatrix4f(stack,"projection", projection);

                environmentModel.draw(stack, configuration.shader);
                droneModel.draw(stack, configuration.shader);
                axisModel.draw(stack, configuration.shader);
                //busterModel.draw(stack, configuration.shader);
            }

            // Update state
            //busterModel.setPosition(new Vector3f(droneStatus.position.x , droneStatus.position.y, droneStatus.position.z));
            //busterModel.setRotation(droneStatus.rotation);

            axisModel.setPosition(new Vector3f(0,0,1));

            droneModel.setPosition(new Vector3f(droneStatus.position.x, droneStatus.position.y, droneStatus.position.z));
            droneModel.setRotation(droneStatus.rotation);


            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void changeCamera() {
        float currTime = (float) glfwGetTime();
        deltaTime = currTime - lastTime;
        lastTime = currTime;
        switch(configuration.type) {
            case DroneCamera -> {
                var cameraOffset = new Vector3f(-3.0f,0,-1.5f);
                var cameraPos = new Vector3f(droneModel.getPosition()).add(cameraOffset);
                camera.setCameraPos(cameraPos);
                camera.setCameraFront(new Vector3f(droneModel.getPosition()).sub(cameraPos).normalize());
                camera.setCameraUp(new Vector3f(0,0,-1));
            }
            case FreeCamera -> {
                camera.setCameraUp(new Vector3f(0,0,-1));
                camera.processInput(window, deltaTime);
            }
            case RacingCamera -> {
                var rot = new Vector3f(droneModel.getRotation());
                var cameraOffset = new Vector3f(-3,0,-1.5f).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(droneModel.getPosition()).add(cameraOffset);
                camera.setCameraPos(cameraPos);
                camera.setCameraFront(droneModel.getPosition().sub(cameraPos).normalize());
                camera.setCameraUp(new Vector3f(0,0,-1).rotate(Convert.toQuaternion(rot)));
            }
            case HorizontalCamera -> {
                var rot = new Vector3f(droneModel.getRotation());
                rot.x = 0;
                rot.y = 0;
                var cameraOffset = new Vector3f(-3,0,-1.5f).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(droneModel.getPosition()).add(cameraOffset);
                camera.setCameraPos(cameraPos);
                camera.setCameraFront(droneModel.getPosition().sub(cameraPos).normalize());
                camera.setCameraUp(new Vector3f(0,0,-1));
            }
            case ObserverCamera -> {
                camera.setCameraPos(new Vector3f());
                camera.setCameraFront(droneModel.getPosition().normalize());
                camera.setCameraUp(new Vector3f(0,0,-1));
            }
        }
    }

    private void changeFog(Shader shader, Shader lightSourceShader) {
        shader.setBool("fog.useFog", configuration.useFog);
        shader.setFloat("fog.density", configuration.fogDensity);
        lightSourceShader.use();
        lightSourceShader.setBool("fog.useFog", configuration.useFog);
        lightSourceShader.setFloat("fog.density", configuration.fogDensity);
        shader.use();
    }

    private void toggleDayNight(Shader shader, Shader lightSourceShader) {

        if(dayFactor <= 0f && !configuration.isDay)
            dayFactor = 0f;
        else if(dayFactor >= 1f && configuration.isDay)
            dayFactor = 1f;
        else if(configuration.isDay)
            dayFactor += 0.04f;
        else
            dayFactor -= 0.04f;

        var skyColor = new Vector3f(DAY_COLOR).mul(dayFactor).add(new Vector3f(NIGHT_COLOR).mul(1 - dayFactor));

        glClearColor(skyColor.x, skyColor.y, skyColor.z, 0.0f);
        shader.setVec3("fog.color", skyColor);
        shader.setVec3("backgroundColor", skyColor);
        lightSourceShader.use();
        lightSourceShader.setVec3("fog.color", skyColor);
        shader.use();
    }

    private void setUpFog(Shader shader) {
        shader.setBool("fog.useFog", configuration.useFog);
        shader.setVec3("fog.color", DAY_COLOR);
        shader.setFloat("fog.density", configuration.fogDensity);
    }

    private void setUpLights(Shader shader) {

        shader.use();
        shader.setVec3("dirLight.direction",  new Vector3f(-0.5f, -0.5f, 0.5f));
        shader.setVec3("dirLight.ambient",  new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setVec3("dirLight.diffuse",  new Vector3f(0.4f, 0.4f, 0.4f));
        shader.setVec3("dirLight.specular",  new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setBool("useDirectionalLight", true);
    }

    @Override
    public void close() {
        positionConsumer.stop();
        propellerConsumer.stop();
    }
}
