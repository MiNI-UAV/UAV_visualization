package org.opengl.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.opengl.OpenGLScene;
import org.opengl.camera.Camera;
import org.opengl.config.Configuration;
import org.opengl.drawable.*;
import org.opengl.input.InputHandler;
import org.opengl.model.DroneStatus;
import org.opengl.model.Model;
import org.opengl.model.ModelOld;
import org.opengl.queue.PositionConsumer;
import org.opengl.shader.Shader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.opengl.importer.GltfImporter.loadModel;

public class Scene {
    private final long window;
    private final int windowHeight;
    private final int windowWidth;
    private final Camera camera;
    private final Configuration configuration;
    private final InputHandler inputHandler;

    private final Vector3f DAY_COLOR = new Vector3f(0.529f, 0.808f, 0.922f);
    private final Vector3f NIGHT_COLOR = new Vector3f(0f, 0f, 0f);
    private float dayFactor = 1.0f;
    //private DrawableDrone drone;
    private DrawableEnvironment environment;
    private DroneStatus droneStatus;
    private Model droneModel, environmentModel, busterModel;
    Shader lightSourceShader;
    private PositionConsumer positionConsumer;
    private PositionConsumer Consumer;

    // camera movement

    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastTime = 0.0f; // Time of last frame


    public Scene(long window, int windowWidth, int windowHeight) throws IOException, URISyntaxException {
        this.window = window;
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        droneStatus = new DroneStatus();
        positionConsumer = new PositionConsumer(droneStatus);

        camera = new Camera();
        configuration = new Configuration();
        inputHandler = new InputHandler(configuration);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(DAY_COLOR.x, DAY_COLOR.y, DAY_COLOR.z, 0.0f);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        setUpDrawables();

        setUpShaders();
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

        droneModel = loadModel("file:///home/faliszewskii/Repositories/opengl-scene/src/main/resources/models/drone.gltf");
        busterModel = loadModel("file:///home/faliszewskii/Repositories/opengl-scene/src/main/resources/models/buster.gltf");
        environmentModel = loadModel("file:///home/faliszewskii/Repositories/opengl-scene/src/main/resources/models/field.gltf");
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

            // BEGIN TRANSFORMATION
            try (MemoryStack stack = MemoryStack.stackPush()) {
                configuration.shader.setVec3("viewPos", camera.getCameraPos());
                configuration.shader.setMatrix4f(stack,"view", view);
                configuration.shader.setMatrix4f(stack,"projection", projection);

                environmentModel.draw(stack, configuration.shader);
                droneModel.draw(stack, configuration.shader);
                busterModel.draw(stack, configuration.shader);
            }
            busterModel.position = new Vector3f(droneStatus.position.x , droneStatus.position.y, droneStatus.position.z - 10);
            busterModel.rotation = droneStatus.rotation;

            droneModel.position = new Vector3f(droneStatus.position.x + 1 , droneStatus.position.y, droneStatus.position.z - 10);
            droneModel.rotation = droneStatus.rotation;

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void changeCamera() {
        switch(configuration.type) {
            case DroneCamera -> {
                var cameraOffset = new Vector3f(-0.6f,0,-0.35f);
                var cameraPos = new Vector3f(busterModel.getPosition()).add(cameraOffset);
                camera.setCameraPos(cameraPos);
                camera.setCameraFront(new Vector3f(busterModel.getPosition()).sub(cameraPos).normalize());
            }
            case FreeCamera -> {
                float currTime = (float) glfwGetTime();
                deltaTime = currTime - lastTime;
                lastTime = currTime;
                camera.processInput(window, deltaTime);
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

}
