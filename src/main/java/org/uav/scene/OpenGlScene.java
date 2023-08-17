package org.uav.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.importer.GltfImporter;
import org.uav.model.Model;
import org.uav.model.SimulationState;
import org.uav.scene.drawable.gui.*;
import org.uav.scene.drawable.gui.widget.map.MapWidget;
import org.uav.scene.shader.Shader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
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

public class OpenGlScene {
    private static final Vector3f DAY_COLOR = new Vector3f(0.529f, 0.808f, 0.922f);

    private final SimulationState simulationState;
    private final Config config;
    private final GltfImporter modelImporter;
    public Shader objectShader;
    public Shader lightSourceShader;
    private List<Model> droneModels;
    private List<Model> projectileModels;
    private Model environmentModel;
    private Gui gui;

    public OpenGlScene(SimulationState simulationState, Config config) throws IOException, URISyntaxException {
        this.config = config;
        this.simulationState = simulationState;

        modelImporter = new GltfImporter();

        droneModels = new ArrayList<>();
        projectileModels = new ArrayList<>();

        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        // https://stackoverflow.com/questions/28079159/opengl-glsl-texture-transparency
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(DAY_COLOR.x, DAY_COLOR.y, DAY_COLOR.z, 0.0f);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

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
        objectShader.setVec3("backgroundColor", DAY_COLOR);
        setUpLights(objectShader);

        String lightSourceVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/lightSourceShader.vert")).getFile();
        String lightSourceFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResource("shaders/lightSourceShader.frag")).getFile();
        lightSourceShader = new Shader(lightSourceVertexShaderSource, lightSourceFragmentShaderSource);
        lightSourceShader.use();
        lightSourceShader.setVec3("lightColor", new Vector3f(1.f, 	1.f, 1.f));
    }

    private void setUpDrawables() throws URISyntaxException, IOException {

        var resourceFile = MessageFormat.format("assets/models/{0}.gltf", config.map);
        var textureDir = MessageFormat.format("assets/textures/{0}", config.map);
        environmentModel = modelImporter.loadModel(resourceFile, textureDir);

        gui = GuiFactory.createStandardGui(simulationState, config);
    }

    private Model createDroneModel() throws URISyntaxException, IOException {
        Supplier<Quaternionf> clockwiseRotation =
                () -> new Quaternionf(0,0,1 * sin(glfwGetTime()*1000),-cos(glfwGetTime()*1000)).normalize();
        Supplier<Quaternionf> counterClockwiseRotation =
                () -> new Quaternionf(0,0,1 * sin(glfwGetTime()*1000),cos(glfwGetTime()*1000)).normalize();
        var droneModel = modelImporter.loadModel("assets/models/drone.gltf", "assets/textures/drone");
        droneModel.setAnimation(null, clockwiseRotation, null, List.of("propeller.2", "propeller.3"));
        droneModel.setAnimation(null, counterClockwiseRotation, null, List.of("propeller.1", "propeller.4"));
        return droneModel;
    }

    private Model createProjectileModel() throws URISyntaxException, IOException {
        return modelImporter.loadModel("assets/models/projectile.gltf", "assets/textures/projectile");
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Matrix4f view = simulationState.getCamera().getViewMatrix();
        Matrix4f projection = new Matrix4f()
                .perspective(toRadians(simulationState.getCamera().getFov()), (float) config.windowWidth / config.windowHeight, 0.1f, 1000f);

        // Drawing
        try (MemoryStack stack = MemoryStack.stackPush()) {
            objectShader.setVec3("viewPos", simulationState.getCamera().getCameraPos());
            objectShader.setMatrix4f(stack,"view", view);
            objectShader.setMatrix4f(stack,"projection", projection);

            environmentModel.draw(stack, objectShader);

            // BEGIN Drone models drawing
            for(Model drone: droneModels) {
                objectShader.setVec3("playerColor", new Vector3f(5,1f,1f));
                drone.draw(stack, objectShader);
                objectShader.setVec3("playerColor", new Vector3f(1));
            }
            if(droneModels.size() != simulationState.getCurrPassDroneStatuses().map.size()) {
                var newDroneModels = new ArrayList<Model>();
                while(newDroneModels.size() != simulationState.getCurrPassDroneStatuses().map.size()) {
                    Model droneModel = createDroneModel();
                    newDroneModels.add(droneModel);
                }
                droneModels = newDroneModels;
            }
            var droneIterator = droneModels.iterator();
            simulationState.getCurrPassDroneStatuses().map.forEach((id, status) -> {
                var model = droneIterator.next();
                model.setPosition(status.position);
                model.setRotation(status.rotation);
            });
            // END Drone models drawing
            // BEGIN Projectiles models drawing
            for(Model projectile: projectileModels) {
                projectile.draw(stack, objectShader);
            }

            if(projectileModels.size() != simulationState.getCurrPassProjectileStatuses().map.size()) {
                var newProjectileModels = new ArrayList<Model>();
                while(newProjectileModels.size() != simulationState.getCurrPassProjectileStatuses().map.size()) {
                    Model projectileModel = createProjectileModel();
                    newProjectileModels.add(projectileModel);
                }
                projectileModels = newProjectileModels;
            }

            var projectileIterator = projectileModels.iterator();
            simulationState.getCurrPassProjectileStatuses().map.forEach((id, status) -> {
                var model = projectileIterator.next();
                model.setPosition(status.position);
            });
            // END Projectiles models drawing

            renderUI(stack);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        glfwSwapBuffers(simulationState.getWindow());
        glfwPollEvents();
    }

    private void renderUI(MemoryStack stack) {
        objectShader.setVec3("viewPos", new Vector3f());
        objectShader.setMatrix4f(stack,"view", new Matrix4f().identity());
        objectShader.setMatrix4f(stack,"projection", new Matrix4f().identity());
        objectShader.setMatrix4f(stack,"model", new Matrix4f().identity());

        ((MapWidget) gui.guiWidget("map")).setHidden(!simulationState.isMapOverlay());
        gui.update();
        gui.draw(objectShader);
    }
}
