package org.uav.presentation.view;

import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.logic.config.Config;
import org.uav.logic.config.DroneParameters;
import org.uav.logic.messages.MessageBoard;
import org.uav.logic.state.simulation.SimulationState;
import org.uav.presentation.entity.abstraction.XMarkEntity;
import org.uav.presentation.entity.bulletTrail.BulletTrailEntity;
import org.uav.presentation.entity.drone.DroneEntity;
import org.uav.presentation.entity.environment.EnvironmentEntity;
import org.uav.presentation.entity.gui.GuiEntity;
import org.uav.presentation.entity.light.DirectionalLight;
import org.uav.presentation.entity.light.PointLight;
import org.uav.presentation.entity.light.SpotLight;
import org.uav.presentation.entity.outline.OutlineEntity;
import org.uav.presentation.entity.projectile.ProjectileEntity;
import org.uav.presentation.entity.rope.Rope;
import org.uav.presentation.entity.rope.RopeEntity;
import org.uav.presentation.entity.weather.Fog;
import org.uav.presentation.model.importer.GltfImporter;
import org.uav.presentation.model.importer.ModelImporter;
import org.uav.presentation.rendering.Shader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Objects;

import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.uav.utils.OpenGLUtils.getSunDirectionVector;

public class OpenGlScene {
    private final SimulationState simulationState;
    private final Config config;
    private Shader objectShader;

    // Shading
    private int depthMapFBO;
    private int depthMap;
    private Shader shadingShader;

    private final DirectionalLight directionalLight;
    private final PointLight pointLight;
    private final SpotLight spotLight;
    private final Fog fog;
    private final DroneEntity droneEntity;
    private final EnvironmentEntity environmentEntity;
    private final ProjectileEntity projectileEntity;
    private final XMarkEntity xMarkEntity;
    private final BulletTrailEntity bulletTrailEntity;
    private final RopeEntity ropeEntity;
    private final GuiEntity guiEntity;
    private final OutlineEntity outlineEntity;

    public OpenGlScene(SimulationState simulationState, Config config, LoadingScreen loadingScreen, DroneParameters droneParameters, MessageBoard messageBoard) throws IOException {
        this.config = config;
        this.simulationState = simulationState;

        fog = new Fog(simulationState.getSkyColor(), config.getSceneSettings().getFogDensity());
        directionalLight = new DirectionalLight(
                getSunDirectionVector(new Vector3f(0,0,1), config.getSceneSettings().getSunAngleYearCycle(), config.getSceneSettings().getSunAngleDayCycle()),
                new Vector3f(0.2f), new Vector3f(0.8f), new Vector3f(0.6f));
        pointLight = PointLight.PointLightFactory.createDronePointLight();
        spotLight = SpotLight.SpotlightFactory.createDroneSpotlight();

        var modelImporter = new ModelImporter(new GltfImporter(loadingScreen, config), simulationState.getAssetsDirectory());
        droneEntity = new DroneEntity(modelImporter.loadModelMap("drones"));
        environmentEntity = new EnvironmentEntity(modelImporter.loadModel(Paths.get("maps", simulationState.getServerMap()).toString()));
        projectileEntity = new ProjectileEntity(simulationState, modelImporter.loadModelMap("projectiles"));
        xMarkEntity = new XMarkEntity(modelImporter.loadModel(Paths.get("core", "xMark").toString()));
        ropeEntity = new RopeEntity(Rope.SEGMENT_COUNT, Rope.THICKNESS, Rope.ROPE_COLOR_1, Rope.ROPE_COLOR_2, directionalLight, simulationState.getSkyColor());
        bulletTrailEntity = new BulletTrailEntity();

        guiEntity = new GuiEntity(simulationState, config, droneParameters, messageBoard);
        outlineEntity = new OutlineEntity(config.getGraphicsSettings().getWindowWidth(), config.getGraphicsSettings().getWindowHeight());

        setUpShaders();
        setUpShadingFrameBuffer();
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

        var shadingVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/shading/shadowShader.vert"));
        var shadingFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/shading/shadowShader.frag"));
        shadingShader = new Shader(shadingVertexShaderSource, shadingFragmentShaderSource);
        shadingShader.use();
    }

    public void render() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            float deltaTimeS = simulationState.getSimulationTimeS() - simulationState.getLastSimulationTimeS();

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
            outlineEntity.generateDroneMask(droneEntity, simulationState, stack, deltaTimeS, getSceneShaderViewMatrix(), getSceneShaderProjectionMatrix());

            // Scene pass
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
            objectShader.use();
            objectShader.setVec3("viewPos", getSceneShaderViewPos());
            objectShader.setMatrix4f(stack,"view", getSceneShaderViewMatrix());
            objectShader.setMatrix4f(stack,"projection", getSceneShaderProjectionMatrix());
            objectShader.setMatrix4f(stack,"directionalLightView", getShadowShaderViewMatrix());
            objectShader.setMatrix4f(stack,"directionalLightProjection", getShadowShaderProjectionMatrix());
            updateLights();
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, depthMap);
            renderScene(stack, objectShader, deltaTimeS);
        }
        // UI pass
        guiEntity.draw(simulationState.isMapOverlay());

        glfwSwapBuffers(simulationState.getWindow());
        glfwPollEvents();
    }

    private void renderScene(MemoryStack stack, Shader shader, float deltaTimeS) {
        var skyColor = simulationState.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 0.0f);

        glStencilMask(0x00);

        environmentEntity.draw(simulationState, stack, shader);
        projectileEntity.draw(stack, shader, simulationState.getCurrPassProjectileStatuses().map.values());
        if(config.getSceneSettings().getDrawInWorldDemandedPositionalCoords())
            xMarkEntity.draw(simulationState.getCurrentControlModeDemanded(), stack, shader);

        glStencilMask(0xFF);
        glStencilFunc(GL_ALWAYS, 1, 0xFF);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        outlineEntity.draw(droneEntity, simulationState, stack, deltaTimeS, getSceneShaderViewMatrix(), getSceneShaderProjectionMatrix());
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        glStencilFunc(GL_EQUAL, 0, 0xFF);
        droneEntity.draw(stack, shader, deltaTimeS, simulationState.getDronesInAir().values(), simulationState.getJoystickStatus());
        glStencilFunc(GL_ALWAYS, 1, 0xFF);

        ropeEntity.draw(
                stack,
                getSceneShaderViewPos(),
                getSceneShaderViewMatrix(),
                getSceneShaderProjectionMatrix(),
                simulationState.getNotifications().ropes,
                simulationState.getDronesInAir(),
                simulationState.getCurrPassProjectileStatuses().map);
        bulletTrailEntity.draw(stack, getSceneShaderViewMatrix(), getSceneShaderProjectionMatrix(), simulationState.getCurrPassProjectileStatuses().map.values(), simulationState);
    }

    private void renderSceneShadows(MemoryStack stack, Shader shader) {
        var skyColor = simulationState.getSkyColor();
        glClearColor(skyColor.x, skyColor.y, skyColor.z, 0.0f);
        float time = simulationState.getSimulationTimeS();
        environmentEntity.draw(simulationState, stack, shader);
        projectileEntity.draw(stack, shader, simulationState.getCurrPassProjectileStatuses().map.values());
        if(config.getSceneSettings().getDrawInWorldDemandedPositionalCoords())
            xMarkEntity.draw(simulationState.getCurrentControlModeDemanded(), stack, shader);
        droneEntity.draw(stack, shader, time, simulationState.getDronesInAir().values(), simulationState.getJoystickStatus());

        ropeEntity.draw(
                stack,
                getSceneShaderViewPos(),
                getSceneShaderViewMatrix(),
                getSceneShaderProjectionMatrix(),
                simulationState.getNotifications().ropes,
                simulationState.getDronesInAir(),
                simulationState.getCurrPassProjectileStatuses().map);
        bulletTrailEntity.draw(stack, getSceneShaderViewMatrix(), getSceneShaderProjectionMatrix(), simulationState.getCurrPassProjectileStatuses().map.values(), simulationState);
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
        var drone = simulationState.getDronesInAir().get(simulationState.getCurrentlyControlledDrone().getId());
        if(drone == null) return new Matrix4f();
        return new Matrix4f().lookAt(
                new Vector3f(drone.droneStatus.position).add( // TODO lookAt function breaks down when looking stright down
                        new Vector3f(0, 0, -config.getGraphicsSettings().getShadowsRenderingDistance()*0.5f)
                                .rotateY((90 - config.getSceneSettings().getSunAngleYearCycle()) / 180 * (float) Math.PI)
                                .rotateX(-config.getSceneSettings().getSunAngleDayCycle() / 180 * (float) Math.PI)
                ),
                new Vector3f(drone.droneStatus.position),
                new Vector3f(0, 0, -1f)
        );
    }

    private void updateLights() {
        var drone = simulationState.getPlayerDrone();

        if(!simulationState.isSpotLightOn() || drone.isEmpty()) {
            spotLight.setSpotLightOn(false);
        } else {
            var droneStatus = drone.get().droneStatus;
            spotLight.setSpotLightOn(true);
            var spotlightPos = new Vector3f(ArrayUtils.toPrimitive(config.getSceneSettings().getCameraFPP(), 0.0F)).rotate(droneStatus.rotation).add(droneStatus.position);
            spotLight.setPosition(spotlightPos);
            spotLight.setDirection(new Vector3f(1, 0, 0).rotate(droneStatus.rotation));
        }
        spotLight.applyTo(objectShader);

        pointLight.setPosition(simulationState.getCamera().getCameraPos());
        pointLight.applyTo(objectShader);
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
}
