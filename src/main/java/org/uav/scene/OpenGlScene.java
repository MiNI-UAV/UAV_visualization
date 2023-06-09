package org.uav.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.config.Config;
import org.uav.importer.GltfImporter;
import org.uav.input.InputHandler;
import org.uav.model.Model;
import org.uav.model.SimulationState;
import org.uav.scene.camera.Camera;
import org.uav.scene.shader.Shader;

import java.io.IOException;
import java.net.URISyntaxException;
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
    private final InputHandler inputHandler;
    private final Camera camera;
    public Shader objectShader;
    public Shader lightSourceShader;
    private List<Model> droneModels;
    private List<Model> projectileModels;
    private Model environmentModel;
    private Model axisModel;

    public OpenGlScene(SimulationState simulationState, Config config) throws IOException, URISyntaxException {
        this.config = config;
        this.simulationState = simulationState;

        modelImporter = new GltfImporter();
        camera = new Camera(simulationState, config);

        droneModels = new ArrayList<>();
        projectileModels = new ArrayList<>();

        inputHandler = new InputHandler(simulationState, config);


        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
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
        environmentModel = modelImporter.loadModel("models/dust.gltf", "textures/dust");
        axisModel = modelImporter.loadModel("models/axis.gltf", "textures/axis");
    }

    private Model createDroneModel() throws URISyntaxException, IOException {
        Supplier<Quaternionf> clockwiseRotation =
                () -> new Quaternionf(0,0,1 * sin(glfwGetTime()*1000),-cos(glfwGetTime()*1000)).normalize();
        Supplier<Quaternionf> counterClockwiseRotation =
                () -> new Quaternionf(0,0,1 * sin(glfwGetTime()*1000),cos(glfwGetTime()*1000)).normalize();
        var droneModel = modelImporter.loadModel("models/drone.gltf", "textures/drone");
        droneModel.setAnimation(null, clockwiseRotation, null, List.of("propeller.2", "propeller.3"));
        droneModel.setAnimation(null, counterClockwiseRotation, null, List.of("propeller.1", "propeller.4"));
        return droneModel;
    }

    private Model createProjectileModel() throws URISyntaxException, IOException {
        return modelImporter.loadModel("models/projectile.gltf", "textures/projectile");
    }

    public void loop() {
        while (!glfwWindowShouldClose(simulationState.getWindow())) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f view = camera.getViewMatrix();
            Matrix4f projection = new Matrix4f().perspective(toRadians(camera.getFov()), (float) config.windowWidth / config.windowHeight, 0.1f, 1000f);

            inputHandler.handleInput(simulationState.getWindow());

            // Drawing
            try (MemoryStack stack = MemoryStack.stackPush()) {
                objectShader.setVec3("viewPos", camera.getCameraPos());
                objectShader.setMatrix4f(stack,"view", view);
                objectShader.setMatrix4f(stack,"projection", projection);

                environmentModel.draw(stack, objectShader);

                // BEGIN Drone models drawing
                for(Model drone: droneModels) {
                    drone.draw(stack, objectShader);
                }
                simulationState.getDroneStatusesMutex().lock();
                if(droneModels.size() != simulationState.getDroneStatuses().map.size()) {
                    var newDroneModels = new ArrayList<Model>();
                    while(newDroneModels.size() != simulationState.getDroneStatuses().map.size()) {
                        Model droneModel = createDroneModel();
                        newDroneModels.add(droneModel);
                    }
                    droneModels = newDroneModels;
                }
                var droneIterator = droneModels.iterator();
                simulationState.getDroneStatuses().map.forEach((id, status) -> {
                    var model = droneIterator.next();
                    model.setPosition(status.position);
                    model.setRotation(status.rotation);
                });
                camera.updateCamera();
                simulationState.getDroneStatusesMutex().unlock();
                // END Drone models drawing
                // BEGIN Projectiles models drawing
                for(Model projectile: projectileModels) {
                    projectile.draw(stack, objectShader);
                }
                simulationState.getProjectileStatusesMutex().lock();

                if(projectileModels.size() != simulationState.getProjectileStatuses().map.size()) {
                    var newProjectileModels = new ArrayList<Model>();
                    while(newProjectileModels.size() != simulationState.getProjectileStatuses().map.size()) {
                        Model projectileModel = createProjectileModel();
                        newProjectileModels.add(projectileModel);
                    }
                    projectileModels = newProjectileModels;
                }

                var projectileIterator = projectileModels.iterator();
                simulationState.getProjectileStatuses().map.forEach((id, status) -> {
                    var model = projectileIterator.next();
                    model.setPosition(status.position);
                });
                simulationState.getProjectileStatusesMutex().unlock();
                // END Projectiles models drawing

                axisModel.setPosition(new Vector3f(0,0,1));
                axisModel.draw(stack, objectShader);
                //busterModel.draw(stack, configuration.shader);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }

            glfwSwapBuffers(simulationState.getWindow());
            glfwPollEvents();
        }
    }
}
