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
import org.opengl.model.Model;
import org.opengl.shader.Shader;

import java.io.IOException;
import java.util.Objects;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

public class Scene {
    private final long window;
    private final int windowHeight;
    private final int windowWidth;
    private final Camera camera;
    private final Configuration configuration;
    private final InputHandler inputHandler;
    private static final int STAR_COUNT = 50;
    private static final int SHADER_OFFSET = 1;

    private final Vector3f DAY_COLOR = new Vector3f(0.529f, 0.808f, 0.922f);
    private final Vector3f NIGHT_COLOR = new Vector3f(0f, 0f, 0f);
    private float dayFactor = 0f;

    private Drawable zeus, jupiter, candleLight, lightOfGabriel, pointyHand, cupcake;
    private DrawableStars stars;
    Shader lightSourceShader;


    public Scene(long window, int windowWidth, int windowHeight) throws IOException {
        this.window = window;
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        camera = new Camera();
        configuration = new Configuration();
        inputHandler = new InputHandler(configuration);

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glClearColor(NIGHT_COLOR.x, NIGHT_COLOR.y, NIGHT_COLOR.z, 0.0f);
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
        configuration.phongShader.setVec3("backgroundColor", NIGHT_COLOR);
        setUpFog(configuration.phongShader);
        setUpLights(configuration.phongShader, stars, candleLight);

        configuration.gouraudShader.use();
        configuration.gouraudShader.setVec3("backgroundColor", NIGHT_COLOR);
        setUpFog(configuration.gouraudShader);
        setUpLights(configuration.gouraudShader, stars, candleLight);

        configuration.flatShader.use();
        configuration.flatShader.setVec3("backgroundColor", NIGHT_COLOR);
        setUpFog(configuration.flatShader);
        setUpLights(configuration.flatShader, stars, candleLight);

        lightSourceShader.use();
        lightSourceShader.setVec3("lightColor", new Vector3f(1.f, 	1.f, 1.f));
        setUpFog(lightSourceShader);
    }

    private void setUpDrawables() {
        Model zeusModel = new Model(Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("models/zeus.obj")).getPath());
        Model jupiterModel = new Model(Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("models/jupiter.obj")).getPath());
        Model star = new Model(Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("models/sun.obj")).getPath());
        Model pointyHandModel = new Model(Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("models/pointyHand.obj")).getPath());
        Model cupcakeModel = new Model(Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("models/cupcake.obj")).getPath());

        zeus = new DrawableZeus(zeusModel);
        jupiter = new DrawableJupiter(jupiterModel, configuration);
        candleLight = new DrawableCandleLight(star);
        lightOfGabriel = new DrawableLightOfGabriel(star);
        stars = new DrawableStars(star, STAR_COUNT, SHADER_OFFSET);
        pointyHand = new DrawablePointyHand(pointyHandModel);
        cupcake = new DrawableCupcake(cupcakeModel);
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

            moveLights(configuration.shader, stars);

            // BEGIN TRANSFORMATION
            try (MemoryStack stack = MemoryStack.stackPush()) {
                configuration.shader.setVec3("viewPos", camera.getCameraPos());
                configuration.shader.setMatrix4f(stack,"view", view);
                configuration.shader.setMatrix4f(stack,"projection", projection);
                lightSourceShader.use();
                lightSourceShader.setMatrix4f(stack,"view", view);
                lightSourceShader.setMatrix4f(stack,"projection", projection);
                lightSourceShader.setVec3("viewPos", camera.getCameraPos());

                zeus.draw(stack, configuration.shader);
                candleLight.draw(stack, lightSourceShader);
                lightOfGabriel.draw(stack, lightSourceShader);
                stars.draw(stack, lightSourceShader);
                jupiter.draw(stack, configuration.shader);
                pointyHand.draw(stack, configuration.shader);
                cupcake.draw(stack, configuration.shader);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void changeCamera() {
        switch(configuration.type) {
            case FrontCamera -> {
                camera.setCameraPos(new Vector3f(0f,0f,-7f));
                camera.setCameraFront(new Vector3f(0, 0, 1));
            }
            case JupiterCamera -> {
                camera.setCameraPos(new Vector3f(0f, 0f, -7f));
                camera.setCameraFront(jupiter.getPosition().sub(new Vector3f(0f, 0f, -7f)));
            }
            case GabrielCamera -> {
                camera.setCameraPos(pointyHand.getPosition());
                camera.setCameraFront(zeus.getPosition().sub(pointyHand.getPosition()));
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
        shader.setVec3("fog.color", NIGHT_COLOR);
        shader.setFloat("fog.density", configuration.fogDensity);
    }

    private void moveLights(Shader shader, DrawableStars stars) {
        moveSpotlights(shader);
        stars.moveStars(shader);
    }

    private void moveSpotlights(Shader shader) {
        for( int i = 0; i< 3; i++)
            shader.setVec3("spotLights[" + i + "].position",  new Vector3f(
                    -2.25f*sin((float)glfwGetTime()),
                    0.1f*sin((float)(PI*glfwGetTime())),
                    -2.25f*cos((float)glfwGetTime())));

        shader.setVec3("spotLights[0].direction",
                new Vector3f(
                        configuration.blueVector.x * sin((float)(glfwGetTime()%(2*PI)-(PI/2))),
                        configuration.blueVector.y,
                        configuration.blueVector.z *cos((float)(glfwGetTime()%(2*PI)-(PI/2))))
                        .sub(new Vector3f(-2.25f*sin((float)glfwGetTime()),0,-2.25f*cos((float)glfwGetTime()))));
        shader.setVec3("spotLights[1].direction",
                new Vector3f(
                        configuration.greenVector.x * sin((float)(glfwGetTime()%(2*PI)-(PI/2))),
                        configuration.greenVector.y,
                        configuration.greenVector.z *cos((float)(glfwGetTime()%(2*PI)-(PI/2))))
                        .sub(new Vector3f(-2.25f*sin((float)glfwGetTime()),0,-2.25f*cos((float)glfwGetTime()))));
        shader.setVec3("spotLights[2].direction",
                new Vector3f(
                        configuration.redVector.x * sin((float)(glfwGetTime()%(2*PI)-(PI/2))),
                        configuration.redVector.y,
                        configuration.redVector.z *cos((float)(glfwGetTime()%(2*PI)-(PI/2))))
                        .sub(new Vector3f(-2.25f*sin((float)glfwGetTime()),0,-2.25f*cos((float)glfwGetTime()))));
    }


    private void setUpLights(Shader shader, DrawableStars stars, Drawable candleLight) {

        shader.setVec3("dirLight.direction",  new Vector3f(-0.2f, -1.0f, -0.3f));
        shader.setVec3("dirLight.ambient",  new Vector3f(0.005f, 0.005f, 0.005f));
        shader.setVec3("dirLight.diffuse",  new Vector3f(0.4f, 0.4f, 0.4f));
        shader.setVec3("dirLight.specular",  new Vector3f(0.5f, 0.5f, 0.5f));

        for(int i=0; i < STAR_COUNT + SHADER_OFFSET; i++){
            shader.setVec3("pointLights[" + i + "].position",
                    i == 0 ? candleLight.getPosition() : stars.getPosition(i - SHADER_OFFSET)
            );
            shader.setVec3("pointLights[" + i + "].ambient", new Vector3f(0f, 0f, 0f));
            shader.setVec3("pointLights[" + i + "].diffuse", new Vector3f(1.f, 	110/255.f, 199/255.f));
            shader.setVec3("pointLights[" + i + "].specular", new Vector3f(1.f, 	110/255.f, 199/255.f));
            shader.setFloat("pointLights[" + i + "].constant", 1.0f);
            shader.setFloat("pointLights[" + i + "].linear", 0.14f);
            shader.setFloat("pointLights[" + i + "].quadratic", 0.07f);
        }

        for(int i=0; i< 3; i++) {
            shader.setVec3("spotLights[" + i + "].position",  new Vector3f(-2.25f*sin((float)glfwGetTime()),0,-2.25f*cos((float)glfwGetTime())));
            shader.setVec3("spotLights[" + i + "].direction", new Vector3f(0f).sub(new Vector3f(-2.25f*sin((float)glfwGetTime()),0,-2.25f*cos((float)glfwGetTime()))));
            shader.setFloat("spotLights[" + i + "].cutOff",   cos(toRadians(8.5f)));
            shader.setFloat("spotLights[" + i + "].outerCutOff",   cos(toRadians(10.5f)));
            shader.setVec3("spotLights[" + i + "].ambient",  new Vector3f(0.0f, 0.0f, 0.0f));
            shader.setFloat("spotLights[" + i + "].constant", 1.0f);
            shader.setFloat("spotLights[" + i + "].linear", 0.027f);
            shader.setFloat("spotLights[" + i + "].quadratic", 0.0028f);
        }

        shader.setVec3("spotLights[0].diffuse",  new Vector3f(0.1f, 0.1f, 1f));
        shader.setVec3("spotLights[0].specular",  new Vector3f(0.1f, 0.1f, 1f));
        shader.setVec3("spotLights[1].diffuse",  new Vector3f(0.1f, 1f, 0.1f));
        shader.setVec3("spotLights[1].specular",  new Vector3f(0.1f, 1f, 0.1f));
        shader.setVec3("spotLights[2].diffuse",  new Vector3f(1f, 0.1f, 0.1f));
        shader.setVec3("spotLights[2].specular",  new Vector3f(1f, 0.1f, 0.1f));
    }

}
