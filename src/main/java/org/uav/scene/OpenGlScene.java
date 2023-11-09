package org.uav.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.importer.GltfImporter;
import org.uav.model.Model;
import org.uav.model.SimulationState;
import org.uav.model.controlMode.ControlModeReply;
import org.uav.model.rope.Rope;
import org.uav.model.rope.RopeModel;
import org.uav.model.status.DroneStatus;
import org.uav.model.status.ProjectileStatus;
import org.uav.scene.gui.Gui;
import org.uav.scene.shader.Shader;
import org.uav.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;

public class OpenGlScene {
    private final static String DEFAULT_DRONE_MODEL = "defaultDrone";
    private final SimulationState simulationState;
    private final Config config;
    private final GltfImporter modelImporter;
    private Shader objectShader;
    private Shader guiShader;
    private Shader ropeShader;
    private Map<String, Model> droneModels;
    private Model projectileModel;
    private Model environmentModel;
    private Model xMarkModel;
    private RopeModel ropeModel;
    private Gui gui;

    // Shading
    private int depthMapFBO;
    private int depthMap;
    private Shader shadingShader;

    public OpenGlScene(SimulationState simulationState, Config config, LoadingScreen loadingScreen, DroneParameters droneParameters) throws IOException {
        this.config = config;
        this.simulationState = simulationState;

        modelImporter = new GltfImporter(loadingScreen, config);

        setUpShaders();
        setUpDrawables(droneParameters);
        setUpShading();
    }

    private void setUpShading() {
        depthMapFBO = glGenFramebuffers();
        depthMap = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthMap);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT,
                config.getGraphicsSettings().getShadowsTextureResolution(),
                config.getGraphicsSettings().getShadowsTextureResolution(),
                0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null
        );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        float[] borderColor = { 1.0f, 1.0f, 1.0f, 1.0f };
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMap, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void setUpLights(Shader shader) {
        shader.use();
        shader.setVec3("dirLight.direction",  new Vector3f(0f, 0f, 1f));
        shader.setVec3("dirLight.ambient",  new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setVec3("dirLight.diffuse",  new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setVec3("dirLight.specular",  new Vector3f(0.5f, 0.5f, 0.5f));
        shader.setBool("useDirectionalLight", true);
    }

    private void setUpShaders() throws IOException {
        var phongVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/phongShader.vert"));
        var phongFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/phongShader.frag"));
        objectShader = new Shader(phongVertexShaderSource, phongFragmentShaderSource);
        objectShader.use();
        objectShader.setVec3("backgroundColor", simulationState.getSkyColor());
        objectShader.setBool("useGammaCorrection", config.getGraphicsSettings().isUseGammaCorrection());
        objectShader.setFloat("gammaCorrection", config.getGraphicsSettings().getGammaCorrection());
        objectShader.setInt("objectTexture", 0);
        objectShader.setInt("shadowMap", 1);
        setUpLights(objectShader);

        var guiVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/guiShader.vert"));
        var guiFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/guiShader.frag"));
        guiShader = new Shader(guiVertexShaderSource, guiFragmentShaderSource);
        guiShader.use();
        guiShader.setBool("useGammaCorrection", config.getGraphicsSettings().isUseGammaCorrection());
        guiShader.setFloat("gammaCorrection", config.getGraphicsSettings().getGammaCorrection());

        var shadingVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/shadingShader.vert"));
        var shadingFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/shadingShader.frag"));
        shadingShader = new Shader(shadingVertexShaderSource, shadingFragmentShaderSource);
        shadingShader.use();

        var ropeVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/ropeShader.vert"));
        var ropeGeometryShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/ropeShader.geom"));
        var ropeFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/ropeShader.frag"));
        ropeShader = new Shader(ropeVertexShaderSource, ropeGeometryShaderSource, ropeFragmentShaderSource);
        ropeShader.use();
        setUpLights(ropeShader);
        ropeShader.setVec3("backgroundColor", simulationState.getSkyColor());
    }

    private void setUpDrawables(DroneParameters droneParameters) throws IOException {

        var mapDir = Paths.get(simulationState.getAssetsDirectory(), "maps", simulationState.getServerMap());
        var modelFile = Paths.get(mapDir.toString(), "model", "model.gltf").toString();
        var textureDir = Paths.get(mapDir.toString(), "textures").toString();
        environmentModel = modelImporter.loadModel(modelFile, textureDir);
        environmentModel.setPosition(new Vector3f());
        environmentModel.setRotation(new Quaternionf());
        createDroneModels();
        projectileModel = loadModel(Paths.get("core", "projectile").toString());
        xMarkModel = loadModel(Paths.get("core", "xMark").toString());
        ropeModel = new RopeModel(
                Rope.SEGMENT_COUNT,
                Rope.THICKNESS,
                ropeShader,
                Rope.ROPE_COLOR_1,
                Rope.ROPE_COLOR_2
        );

        gui = new Gui(simulationState, config, droneParameters);
    }

    private void createDroneModels() throws IOException {
        droneModels = new HashMap<>();
        var droneDirPath = Paths.get(simulationState.getAssetsDirectory(), "drones").toString();
        File droneDirectory = new File(droneDirPath);
        for(File drone: Objects.requireNonNull(droneDirectory.listFiles())) {
            var modelFile = Paths.get(drone.getAbsolutePath(), "model", "model.gltf").toString();
            var textureDir = Paths.get(drone.getAbsolutePath(), "textures").toString();
            var droneModel = modelImporter.loadModel(modelFile, textureDir);
            droneModels.put(drone.getName(), droneModel);
        }
    }

    private Model loadModel(String dir) throws IOException {
        String modelDir = Paths.get(simulationState.getAssetsDirectory(), dir).toString();
        return modelImporter.loadModel(Paths.get(modelDir, "model", "model.gltf").toString(), Paths.get(modelDir, "textures").toString());
    }

    public void render() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Shading pass
            glViewport(0, 0, config.getGraphicsSettings().getShadowsTextureResolution(), config.getGraphicsSettings().getShadowsTextureResolution());
            glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
            glClear(GL_DEPTH_BUFFER_BIT);
            prepareShadingShader(stack, getShadowShaderViewMatrix(), getShadowShaderProjectionMatrix());
            renderScene(stack, shadingShader);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);

            // Scene pass
            glViewport(0, 0, config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            prepareSceneShader(
                    stack,
                    getSceneShaderViewPos(),
                    getSceneShaderViewMatrix(),
                    getSceneShaderProjectionMatrix(),
                    getShadowShaderViewMatrix(),
                    getShadowShaderProjectionMatrix()
            );
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, depthMap);
            renderScene(stack, objectShader);
        }
        // UI pass
        renderUI();

        glfwSwapBuffers(simulationState.getWindow());
        glfwPollEvents();
    }

    private void prepareShadingShader(MemoryStack stack, Matrix4f view, Matrix4f projection) {
        shadingShader.use();
        shadingShader.setMatrix4f(stack,"view", view);
        shadingShader.setMatrix4f(stack,"projection", projection);
    }

    private Matrix4f getShadowShaderProjectionMatrix() {
        float near_plane = 100f;
        float far_plane = config.getGraphicsSettings().getShadowsRenderingDistance();
        return new Matrix4f().ortho(
                -config.getGraphicsSettings().getShadowsRenderingDistance(),
                config.getGraphicsSettings().getShadowsRenderingDistance(),
                -config.getGraphicsSettings().getShadowsRenderingDistance(),
                config.getGraphicsSettings().getShadowsRenderingDistance(),
                near_plane,
                far_plane
        );
        //return new Matrix4f().perspective(90, 1f, near_plane, far_plane);
    }

    private Matrix4f getShadowShaderViewMatrix() {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().getId());
        if(drone == null) return new Matrix4f();
        return new Matrix4f().lookAt(
                new Vector3f(drone.position).add( // TODO lookAt function breaks down when looking stright down
                        new Vector3f(0, 0, -config.getGraphicsSettings().getShadowsRenderingDistance()*0.5f)
                                .rotateY((90 - config.getSceneSettings().getSunAngleYearCycle()) / 180 * (float) Math.PI)
                                .rotateX(-config.getSceneSettings().getSunAngleDayCycle() / 180 * (float) Math.PI)
                ),
                new Vector3f(drone.position),
                new Vector3f(0, 0, -1f)
        );
    }

    private void prepareSceneShader(
            MemoryStack stack,
            Vector3f viewPos,
            Matrix4f view,
            Matrix4f projection,
            Matrix4f directionalLightView,
            Matrix4f directionalLightProjection
    ) {
        objectShader.use();
        objectShader.setVec3("viewPos", viewPos);
        objectShader.setMatrix4f(stack,"view", view);
        objectShader.setMatrix4f(stack,"projection", projection);
        objectShader.setMatrix4f(stack,"directionalLightView", directionalLightView);
        objectShader.setMatrix4f(stack,"directionalLightProjection", directionalLightProjection);
        ropeShader.use();
        ropeShader.setVec3("viewPos", viewPos);
        ropeShader.setMatrix4f(stack,"view", view);
        ropeShader.setMatrix4f(stack,"projection", projection);
    }

    private Matrix4f getSceneShaderProjectionMatrix() {
        return new Matrix4f()
                .perspective(
                        toRadians(simulationState.getCamera().getFov()),
                        (float) config.getGraphicsSettings().getWindowWidth() / config.getGraphicsSettings().getWindowHeight(),
                        0.1f,
                        1000f
                );
    }

    private Matrix4f getSceneShaderViewMatrix() {
        return simulationState.getCamera().getViewMatrix();
    }

    private Vector3f getSceneShaderViewPos() {
        return simulationState.getCamera().getCameraPos();
    }

    private void renderScene(MemoryStack stack, Shader shader) {
        var skyColor = simulationState.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 0.0f);
        float time = simulationState.getSimulationTimeS();
        var renderQueue = new RenderQueue(simulationState.getCamera().getCameraPos());
        environmentModel.addToQueue(renderQueue, shader, time);
        addDronesToQueue(renderQueue, shader, time);
        addProjectilesToQueue(renderQueue, shader, time);
        addXMarkToQueue(renderQueue, shader, time);
        renderQueue.render(stack);
        drawRopes();
    }

    private void drawRopes() {
        for (Rope rope: simulationState.getNotifications().ropes) {
            if(
                !simulationState.getCurrPassDroneStatuses().map.containsKey(rope.ownerId) ||
                !simulationState.getCurrPassProjectileStatuses().map.containsKey(rope.objectId)
            )
                continue;
            var owner = simulationState.getCurrPassDroneStatuses().map.get(rope.ownerId);
            var object = simulationState.getCurrPassProjectileStatuses().map.get(rope.objectId);
            ropeModel.setParameters(new Vector3f(owner.position).add(rope.ownerOffset), object.position, rope.ropeLength);
            ropeModel.draw();
        }
    }

    private void addDronesToQueue(RenderQueue renderQueue, Shader shader, float time) {
        for(DroneStatus status: simulationState.getCurrPassDroneStatuses().map.values()) {
            String currentDroneModelName = simulationState.getNotifications().droneModels.getOrDefault(status.id, DEFAULT_DRONE_MODEL);
            Model currentDroneModel = droneModels.getOrDefault(currentDroneModelName, droneModels.get(DEFAULT_DRONE_MODEL));
            currentDroneModel.addToQueue(renderQueue, shader, time);
            currentDroneModel.setPosition(status.position);
            currentDroneModel.setRotation(status.rotation);
        }
    }

    private void addProjectilesToQueue(RenderQueue renderQueue, Shader shader, float time) {
        for(ProjectileStatus status: simulationState.getCurrPassProjectileStatuses().map.values()) {
            projectileModel.addToQueue(renderQueue, shader, time);
            projectileModel.setPosition(status.position);
            projectileModel.setRotation(new Quaternionf());
        }
    }

    private void addXMarkToQueue(RenderQueue renderQueue, Shader shader, float time) {
        if(
            config.getSceneSettings().isDrawInWorldDemandedPositionalCoords() &&
            simulationState.getCurrentControlModeDemanded() != null &&
            simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.X) &&
            simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.Y) &&
            simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.Z) &&
            simulationState.getCurrentControlModeDemanded().demanded.containsKey(ControlModeReply.YAW)
        ){
            float demandedX = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.X);
            float demandedY = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.Y);
            float demandedZ = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.Z);
            float demandedYaw = simulationState.getCurrentControlModeDemanded().demanded.get(ControlModeReply.YAW);
            xMarkModel.setPosition(new Vector3f(demandedX, demandedY, demandedZ));
            xMarkModel.setRotation(Convert.toQuaternion(new Vector3f(0, 0, demandedYaw)));
            xMarkModel.addToQueue(renderQueue, shader, time);
        }
    }

    private void renderUI() {
        gui.openMap(simulationState.isMapOverlay());
        gui.update();
        gui.draw(guiShader);
    }
}
