package org.uav.scene;

import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.config.DroneParameters;
import org.uav.importer.GltfImporter;
import org.uav.importer.ModelImporter;
import org.uav.model.SimulationState;
import org.uav.model.rope.Rope;
import org.uav.model.rope.RopeModel;
import org.uav.model.status.ProjectileStatus;
import org.uav.scene.bullet.BulletTrail;
import org.uav.scene.gui.Gui;
import org.uav.scene.shader.Shader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.uav.utils.OpenGLUtils.drawWithDepthFunc;
import static org.uav.utils.OpenGLUtils.getSunDirectionVector;

public class OpenGlScene {
    private static final String DEFAULT_PROJECTILE_MODEL = "defaultProjectile";
    private final SimulationState simulationState;
    private final Config config;
    private Shader objectShader;
    private Shader guiShader;
    private Shader ropeShader;
    private Shader bulletTrailShader;
    private RopeModel ropeModel;
    private HashMap<Integer, BulletTrail> bulletTrails;
    private Gui gui;
    private final Outline outline;

    // Shading
    private int depthMapFBO;
    private int depthMap;
    private Shader shadingShader;

    private final DirectionalLight directionalLight;
    private final SpotLight spotLight;
    private final Fog fog;
    private final DroneEntity droneEntity;
    private final EnvironmentEntity environmentEntity;
    private final ProjectileEntity projectileEntity;
    private final XMarkEntity xMarkEntity;

    public OpenGlScene(SimulationState simulationState, Config config, LoadingScreen loadingScreen, DroneParameters droneParameters) throws IOException {
        this.config = config;
        this.simulationState = simulationState;

        var modelImporter = new ModelImporter(new GltfImporter(loadingScreen, config), simulationState.getAssetsDirectory());
        fog = new Fog(simulationState.getSkyColor(), config.getSceneSettings().getFogDensity());
        directionalLight = new DirectionalLight(
                getSunDirectionVector(new Vector3f(0,0,1), config.getSceneSettings().getSunAngleYearCycle(), config.getSceneSettings().getSunAngleDayCycle()),
                new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.5f, 0.5f, 0.5f));
        spotLight = SpotLight.SpotlightFactory.createDroneSpotlight();
        droneEntity = new DroneEntity(simulationState, modelImporter.loadModelMap("drones"));
        environmentEntity = new EnvironmentEntity(modelImporter.loadModel(Paths.get("maps", simulationState.getServerMap()).toString()));
        projectileEntity = new ProjectileEntity(simulationState, modelImporter.loadModelMap("projectiles"));
        xMarkEntity = new XMarkEntity(modelImporter.loadModel(Paths.get("core", "xMark").toString()));

        setUpShaders();
        setUpDrawables(droneParameters);
        setUpShadingFrameBuffer();
        outline = new Outline(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight());
    }

    private void setUpShadingFrameBuffer() {
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

    private void setUpShaders() throws IOException {
        var phongVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/models/phongShader.vert"));
        var phongFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/models/phongShader.frag"));
        objectShader = new Shader(phongVertexShaderSource, phongFragmentShaderSource);
        objectShader.use();
        objectShader.setVec3("backgroundColor", simulationState.getSkyColor());
        objectShader.setBool("useGammaCorrection", config.getGraphicsSettings().getUseGammaCorrection());
        objectShader.setFloat("gammaCorrection", config.getGraphicsSettings().getGammaCorrection());
        objectShader.setInt("objectTexture", 0);
        objectShader.setInt("shadowMap", 1);
        directionalLight.applyTo(objectShader);
        spotLight.applyTo(objectShader);
        fog.applyTo(objectShader);

        var guiVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/gui/guiShader.vert"));
        var guiFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/gui/guiShader.frag"));
        guiShader = new Shader(guiVertexShaderSource, guiFragmentShaderSource);
        guiShader.use();
        guiShader.setBool("useGammaCorrection", config.getGraphicsSettings().getUseGammaCorrection());
        guiShader.setFloat("gammaCorrection", config.getGraphicsSettings().getGammaCorrection());

        var shadingVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/shading/shadowShader.vert"));
        var shadingFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/shading/shadowShader.frag"));
        shadingShader = new Shader(shadingVertexShaderSource, shadingFragmentShaderSource);
        shadingShader.use();

        var ropeVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/rope/ropeShader.vert"));
        var ropeGeometryShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/rope/ropeShader.geom"));
        var ropeFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/rope/ropeShader.frag"));
        ropeShader = new Shader(ropeVertexShaderSource, ropeGeometryShaderSource, ropeFragmentShaderSource);
        ropeShader.use();
        directionalLight.applyTo(ropeShader);
        ropeShader.setVec3("backgroundColor", simulationState.getSkyColor());

        var bulletTrailVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/bullets/bulletTrailShader.vert"));
        var bulletTrailGeometryShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/bullets/bulletTrailShader.geom"));
        var bulletTrailFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/bullets/bulletTrailShader.frag"));
        bulletTrailShader = new Shader(bulletTrailVertexShaderSource, bulletTrailGeometryShaderSource, bulletTrailFragmentShaderSource);
        bulletTrailShader.use();
        bulletTrailShader.setVec3("color", new Vector3f(1,1,1));
        bulletTrailShader.setFloat("startingOpacity", 1f);
    }

    private void setUpDrawables(DroneParameters droneParameters) {
        ropeModel = new RopeModel(
                Rope.SEGMENT_COUNT,
                Rope.THICKNESS,
                ropeShader,
                Rope.ROPE_COLOR_1,
                Rope.ROPE_COLOR_2
        );

        gui = new Gui(simulationState, config, droneParameters);
        bulletTrails = new HashMap<>();
    }

    public void render() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            float time = simulationState.getSimulationTimeS();

            // Shading pass
            if(config.getGraphicsSettings().getUseShadows()) {
                glViewport(0, 0, config.getGraphicsSettings().getShadowsTextureResolution(), config.getGraphicsSettings().getShadowsTextureResolution());
                glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
                glClear(GL_DEPTH_BUFFER_BIT);
                prepareShadingShader(stack, getShadowShaderViewMatrix(), getShadowShaderProjectionMatrix());
                renderSceneShadows(stack, shadingShader);
                glBindFramebuffer(GL_FRAMEBUFFER, 0);
                glViewport(0, 0, config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight());
            }

            // Drone Mask
            outline.prepareFlatShader(stack, getSceneShaderViewMatrix(), getSceneShaderProjectionMatrix());
            outline.generateDroneMask(droneEntity, stack, time);

            // Scene pass
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
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
            renderScene(stack, objectShader, time);
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
        updateLights();
        ropeShader.use();
        ropeShader.setVec3("viewPos", viewPos);
        ropeShader.setMatrix4f(stack,"view", view);
        ropeShader.setMatrix4f(stack,"projection", projection);
        bulletTrailShader.use();
        bulletTrailShader.setMatrix4f(stack,"view", view);
        bulletTrailShader.setMatrix4f(stack,"projection", projection);
        outline.prepareOutlineShader(stack, getSceneShaderViewMatrix(), getSceneShaderProjectionMatrix());
    }

    private void updateLights() {
        var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().getId());
        if(!simulationState.isSpotLightOn() || drone == null) {
            spotLight.setSpotLightOn(false);
        } else {
            spotLight.setSpotLightOn(true);
            var spotlightPos = new Vector3f(ArrayUtils.toPrimitive(config.getSceneSettings().getCameraFPP(), 0.0F)).rotate(drone.rotation).add(drone.position);
            spotLight.setPosition(spotlightPos);
            spotLight.setDirection(new Vector3f(1, 0, 0).rotate(drone.rotation));
        }
        spotLight.applyTo(objectShader);
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

    private void renderScene(MemoryStack stack, Shader shader, float time) {
        var skyColor = simulationState.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 0.0f);

        glStencilMask(0x00);

        environmentEntity.draw(simulationState, stack, shader, time);
        projectileEntity.draw(stack, shader, time, simulationState.getCurrPassProjectileStatuses().map.values());
        if(config.getSceneSettings().getDrawInWorldDemandedPositionalCoords())
            xMarkEntity.draw(simulationState.getCurrentControlModeDemanded(), stack, shader, time);

        glStencilMask(0xFF);
        glStencilFunc(GL_ALWAYS, 1, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        outline.draw(droneEntity, stack, time);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        glStencilFunc(GL_EQUAL, 0, 0xFF);
        droneEntity.draw(stack, shader, time, simulationState.getCurrPassDroneStatuses().map.values());
        glStencilFunc(GL_ALWAYS, 1, 0xFF);

        drawRopes();
        drawProjectileTrails(stack);
    }

    private void renderSceneShadows(MemoryStack stack, Shader shader) {
        var skyColor = simulationState.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 0.0f);
        float time = simulationState.getSimulationTimeS();
        environmentEntity.draw(simulationState, stack, shader, time);
        projectileEntity.draw(stack, shader, time, simulationState.getCurrPassProjectileStatuses().map.values());
        if(config.getSceneSettings().getDrawInWorldDemandedPositionalCoords())
            xMarkEntity.draw(simulationState.getCurrentControlModeDemanded(), stack, shader, time);
        droneEntity.draw(stack, shader, time, simulationState.getCurrPassDroneStatuses().map.values());
        drawRopes();
        drawProjectileTrails(stack);
    }

    private void drawProjectileTrails(MemoryStack stack) {
        HashMap<Integer, BulletTrail> newBulletTrails = new HashMap<>();
        for(ProjectileStatus status: simulationState.getCurrPassProjectileStatuses().map.values()) {
            BulletTrail bt;
            if(bulletTrails.containsKey(status.id)) {
                bt = bulletTrails.get(status.id);
            } else {
                bt = new BulletTrail(bulletTrailShader);
                var drone = simulationState.getCurrPassDroneStatuses().map.get(simulationState.getCurrentlyControlledDrone().getId());
                if(drone != null)
                    bt.addPoint(drone.position);
            }
            bt.addPoint(status.position);
            newBulletTrails.put(status.id, bt);
            bt.draw(stack);
        }
        bulletTrails = newBulletTrails;
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

    private void renderUI() {
        gui.openMap(simulationState.isMapOverlay());
        gui.update();
        drawWithDepthFunc(() -> gui.draw(guiShader), GL_ALWAYS);
    }
}
