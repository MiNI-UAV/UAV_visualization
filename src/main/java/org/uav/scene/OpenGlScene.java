package org.uav.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.importer.GltfImporter;
import org.uav.model.Model;
import org.uav.model.SimulationState;
import org.uav.model.status.DroneStatus;
import org.uav.model.status.ProjectileStatus;
import org.uav.queue.ControlMode;
import org.uav.scene.drawable.gui.Gui;
import org.uav.scene.shader.Shader;
import org.uav.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;

public class OpenGlScene {
    private final static String DEFAULT_DRONE_MODEL = "defaultDrone";
    private final SimulationState simulationState;
    private final Config config;
    private final GltfImporter modelImporter;
    private Shader objectShader;
    private Shader lightSourceShader;
    private Shader guiShader;
    private Map<String, Model> droneModels;
    private Model projectileModel;
    private Model environmentModel;
    private Model xMarkModel;
    private Gui gui;

    public OpenGlScene(SimulationState simulationState, Config config, LoadingScreen loadingScreen, DroneParameters droneParameters) throws IOException, URISyntaxException {
        this.config = config;
        this.simulationState = simulationState;

        modelImporter = new GltfImporter(loadingScreen);

        setUpDrawables(droneParameters);
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
        var phongVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/phongShader.vert"));
        var phongFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/phongShader.frag"));
        objectShader = new Shader(phongVertexShaderSource, phongFragmentShaderSource);
        objectShader.use();
        objectShader.setVec3("backgroundColor", simulationState.getSkyColor());
        setUpLights(objectShader);

        var lightSourceVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/lightSourceShader.vert"));
        var lightSourceFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/lightSourceShader.frag"));
        lightSourceShader = new Shader(lightSourceVertexShaderSource, lightSourceFragmentShaderSource);
        lightSourceShader.use();
        lightSourceShader.setVec3("lightColor", new Vector3f(1.f, 	1.f, 1.f));

        var guiVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/guiShader.vert"));
        var guiFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/guiShader.frag"));
        guiShader = new Shader(guiVertexShaderSource, guiFragmentShaderSource);
        guiShader.use();
    }

    private void setUpDrawables(DroneParameters droneParameters) throws URISyntaxException, IOException {

        var mapDir = simulationState.getAssetsDirectory() + "/maps/" + simulationState.getServerMap();
        var modelFile = mapDir + "/model/model.gltf";
        var textureDir = mapDir + "/textures";
        environmentModel = modelImporter.loadModel(modelFile, textureDir);
        environmentModel.setPosition(new Vector3f());
        environmentModel.setRotation(new Quaternionf());
        createDroneModels();
        projectileModel = loadModel("/core/projectile");
        xMarkModel = loadModel("/core/xMark");

        gui = new Gui(simulationState, config, droneParameters);
    }

    private void createDroneModels() throws URISyntaxException, IOException {
        droneModels = new HashMap<>();
        var droneDirPath = simulationState.getAssetsDirectory() + "/drones";
        File droneDirectory = new File(droneDirPath);
        for(File drone: Objects.requireNonNull(droneDirectory.listFiles())) {
            var modelFile = drone + "/model/model.gltf";
            var textureDir = drone + "/textures";
            var droneModel = modelImporter.loadModel(modelFile, textureDir);
            droneModels.put(drone.getName(), droneModel);
        }
    }

    private Model loadModel(String dir) throws URISyntaxException, IOException {
        String modelDir = simulationState.getAssetsDirectory() + dir;
        return modelImporter.loadModel(modelDir + "/model/model.gltf", modelDir + "/textures");
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        var skyColor = simulationState.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 0.0f);

        Matrix4f view = simulationState.getCamera().getViewMatrix();
        Matrix4f projection = new Matrix4f()
                .perspective(toRadians(simulationState.getCamera().getFov()), (float) config.getWindowWidth() / config.getWindowHeight(), 0.1f, 1000f);

        // Drawing
        try (MemoryStack stack = MemoryStack.stackPush()) {
            objectShader.use();
            objectShader.setVec3("viewPos", simulationState.getCamera().getCameraPos());
            objectShader.setMatrix4f(stack,"view", view);
            objectShader.setMatrix4f(stack,"projection", projection);

            environmentModel.draw(stack, objectShader, simulationState.getSimulationTime());

            for(DroneStatus status: simulationState.getCurrPassDroneStatuses().map.values()) {
                String currentDroneModelName = simulationState.getNotifications().droneModels.getOrDefault(status.id, DEFAULT_DRONE_MODEL);
                Model currentDroneModel = droneModels.getOrDefault(currentDroneModelName, droneModels.get(DEFAULT_DRONE_MODEL));
                currentDroneModel.draw(stack, objectShader, simulationState.getSimulationTime());
                currentDroneModel.setPosition(status.position);
                currentDroneModel.setRotation(status.rotation);
            }
            for(ProjectileStatus status: simulationState.getCurrPassProjectileStatuses().map.values()) {
                projectileModel.draw(stack, objectShader, simulationState.getSimulationTime());
                projectileModel.setPosition(status.position);
                projectileModel.setRotation(new Quaternionf());
            }
            if(
                config.isDrawInWorldDemandedPositionalCoords()
                && simulationState.getCurrentControlMode() == ControlMode.Positional
            ) {
                Vector4f demanded = simulationState.getPositionalModeDemands();
                if(demanded != null) {
                    xMarkModel.setPosition(new Vector3f(demanded.x, demanded.y, demanded.z));
                    xMarkModel.setRotation(Convert.toQuaternion(new Vector3f(0, 0, demanded.w)));
                    xMarkModel.draw(stack, objectShader, simulationState.getSimulationTime());
                }

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
