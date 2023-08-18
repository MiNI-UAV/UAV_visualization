package org.uav.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.importer.GltfImporter;
import org.uav.model.Model;
import org.uav.model.SimulationState;
import org.uav.model.status.DroneStatus;
import org.uav.model.status.ProjectileStatus;
import org.uav.scene.drawable.gui.Gui;
import org.uav.scene.shader.Shader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class OpenGlScene {
    private final SimulationState simulationState;
    private final Config config;
    private final GltfImporter modelImporter;
    private Shader objectShader;
    private Shader lightSourceShader;
    private Shader guiShader;
    private Model droneModel;
    private Model projectileModel;
    private Model environmentModel;
    private Gui gui;

    public OpenGlScene(SimulationState simulationState, Config config, LoadingScreen loadingScreen) throws IOException, URISyntaxException {
        this.config = config;
        this.simulationState = simulationState;

        modelImporter = new GltfImporter(loadingScreen);

        setUpDrawables();
        setUpShaders();
    }

    private void setUpLights(Shader shader) {
        shader.use();
        shader.setVec3("dirLight.direction",  new Vector3f(-0.5f, -0.5f, 0.5f));
        shader.setVec3("dirLight.ambient",  new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setVec3("dirLight.diffuse",  new Vector3f(0.4f, 0.4f, 0.4f));
        shader.setVec3("dirLight.specular",  new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setBool("useDirectionalLight", true);
    }

    private void setUpShaders() throws IOException {
        String phongVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/phongShader.vert")).getFile();
        String phongFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/phongShader.frag")).getFile();
        objectShader = new Shader(phongVertexShaderSource, phongFragmentShaderSource);
        objectShader.use();
        objectShader.setVec3("backgroundColor", simulationState.getSkyColor());
        setUpLights(objectShader);

        String lightSourceVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/lightSourceShader.vert")).getFile();
        String lightSourceFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/lightSourceShader.frag")).getFile();
        lightSourceShader = new Shader(lightSourceVertexShaderSource, lightSourceFragmentShaderSource);
        lightSourceShader.use();
        lightSourceShader.setVec3("lightColor", new Vector3f(1.f, 	1.f, 1.f));

        String guiVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/guiShader.vert")).getFile();
        String guiFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/guiShader.frag")).getFile();
        guiShader = new Shader(guiVertexShaderSource, guiFragmentShaderSource);
        guiShader.use();
    }

    private void setUpDrawables() throws URISyntaxException, IOException {

        var mapDir = simulationState.getAssetsDirectory() + "/maps/" + simulationState.getServerMap();
        var modelFile = mapDir + "/model/model.gltf";
        var textureDir = mapDir + "/textures";
        environmentModel = modelImporter.loadModel(modelFile, textureDir);
        droneModel = createDroneModel();
        projectileModel = createProjectileModel();

        gui = new Gui(simulationState, config);
    }

    private Model createDroneModel() throws URISyntaxException, IOException {
        Supplier<Quaternionf> clockwiseRotation =
                () -> new Quaternionf(0,0,1 * sin(glfwGetTime()*1000),-cos(glfwGetTime()*1000)).normalize();
        Supplier<Quaternionf> counterClockwiseRotation =
                () -> new Quaternionf(0,0,1 * sin(glfwGetTime()*1000),cos(glfwGetTime()*1000)).normalize();
        var droneDir = simulationState.getAssetsDirectory() + "/drones/" + config.droneModel;
        var modelFile = droneDir + "/model/model.gltf";
        var textureDir = droneDir + "/textures";
        var droneModel = modelImporter.loadModel(modelFile, textureDir);
        droneModel.setAnimation(null, clockwiseRotation, null, List.of("propeller.2", "propeller.3"));
        droneModel.setAnimation(null, counterClockwiseRotation, null, List.of("propeller.1", "propeller.4"));
        return droneModel;
    }

    private Model createProjectileModel() throws URISyntaxException, IOException {
        String projectileDir = simulationState.getAssetsDirectory() + "/core/projectile";
        return modelImporter.loadModel(projectileDir + "/model/model.gltf", projectileDir + "/textures");
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        var skyColor = simulationState.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 0.0f);

        Matrix4f view = simulationState.getCamera().getViewMatrix();
        Matrix4f projection = new Matrix4f()
                .perspective(toRadians(simulationState.getCamera().getFov()), (float) config.windowWidth / config.windowHeight, 0.1f, 1000f);

        // Drawing
        try (MemoryStack stack = MemoryStack.stackPush()) {
            objectShader.use();
            objectShader.setVec3("viewPos", simulationState.getCamera().getCameraPos());
            objectShader.setMatrix4f(stack,"view", view);
            objectShader.setMatrix4f(stack,"projection", projection);

            environmentModel.draw(stack, objectShader);

            for(DroneStatus status: simulationState.getCurrPassDroneStatuses().map.values()) {
                droneModel.draw(stack, objectShader);
                droneModel.setPosition(status.position);
                droneModel.setRotation(status.rotation);
            }
            for(ProjectileStatus status: simulationState.getCurrPassProjectileStatuses().map.values()) {
                projectileModel.draw(stack, objectShader);
                projectileModel.setPosition(status.position);
            }

            renderUI();
        }

        glfwSwapBuffers(simulationState.getWindow());
        glfwPollEvents();
    }

    private void renderUI() {
        gui.openMap(simulationState.isMapOverlay());
        gui.update();
        gui.draw(guiShader);
    }
}
