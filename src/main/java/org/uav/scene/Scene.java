package org.uav.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.uav.OpenGLScene;
import org.uav.camera.Camera;
import org.uav.config.Configuration;
import org.uav.importer.GltfImporter;
import org.uav.input.InputHandler;
import org.uav.input.JoystickButtonFunctions;
import org.uav.model.*;
import org.uav.model.status.DroneStatuses;
import org.uav.model.status.ProjectileStatuses;
import org.uav.queue.Actions;
import org.uav.queue.DroneRequester;
import org.uav.queue.ProjectileStatusesConsumer;
import org.uav.queue.DroneStatusConsumer;
import org.uav.shader.Shader;
import org.uav.utils.Convert;
import org.zeromq.ZContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.joml.Math.toRadians;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;

public class Scene implements AutoCloseable {
    private final long window;
    private final int windowHeight;
    private final int windowWidth;
    public Shader objectShader;
    public Shader lightSourceShader;
    private final Camera camera;
    private final Configuration configuration;
    private final InputHandler inputHandler;

    private final Vector3f DAY_COLOR = new Vector3f(0.529f, 0.808f, 0.922f);
    private final Drone controlledDrone;
    private List<Model> droneModels;
    private final DroneStatuses droneStatuses;
    private final ReentrantLock droneStatusesMutex;
    private final DroneStatusConsumer droneStatusConsumer;
    private List<Model> projectileModels;
    private final ProjectileStatuses projectileStatuses;
    private final ReentrantLock projectileStatusesMutex;
    private final ProjectileStatusesConsumer projectileStatusesConsumer;
    private Model environmentModel;
    private Model axisModel;
    private final DroneRequester droneRequester;
    private final GltfImporter modelImporter;

    // camera movement

    static float deltaTime = 0.0f;    // Time between current frame and last frame
    static float lastTime = 0.0f; // Time of last frame


    public Scene(long window, int windowWidth, int windowHeight) throws IOException, URISyntaxException {
        this.window = window;
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        modelImporter = new GltfImporter();
        ZContext context = new ZContext();
        camera = new Camera();
        configuration = new Configuration();

        droneStatuses = new DroneStatuses();
        droneModels = new ArrayList<>();
        droneStatusesMutex = new ReentrantLock();
        droneStatusConsumer = new DroneStatusConsumer(context, droneStatuses, droneStatusesMutex, configuration);
        droneStatusConsumer.start();

        projectileStatuses = new ProjectileStatuses();
        projectileModels = new ArrayList<>();
        projectileStatusesMutex = new ReentrantLock();
        projectileStatusesConsumer = new ProjectileStatusesConsumer(context, projectileStatuses, projectileStatusesMutex, configuration);
        projectileStatusesConsumer.start();

        droneRequester = new DroneRequester(context, configuration);
        var newDroneResult = droneRequester.requestNewDrone(configuration.droneName);
        controlledDrone = newDroneResult.orElseThrow();

        inputHandler = new InputHandler(configuration, controlledDrone);

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
//        //XBOX Igor
        joystickMapping.put(0, 0);
        joystickMapping.put(1, 1);
        joystickMapping.put(3, 2);
        joystickMapping.put(4, 3);
        // XBOX Wojtek
//        joystickMapping.put(0, 0);
//        joystickMapping.put(1, 1);
//        joystickMapping.put(2, 2);
//        joystickMapping.put(3, 3);

        var joystickInversionMapping = new HashMap<Integer, Boolean>();
        //HOTAS
        // joystickInversionMapping.put(5, false);
        // joystickInversionMapping.put(2, true);
        // joystickInversionMapping.put(0, false);
        // joystickInversionMapping.put(1, true);
        //XBOX Igor
        joystickInversionMapping.put(0, false);
        joystickInversionMapping.put(1, true);
        joystickInversionMapping.put(4, true);
        joystickInversionMapping.put(3, false);
        // XBOX Wojtek
//        joystickInversionMapping.put(0, false);
//        joystickInversionMapping.put(1, true);
//        joystickInversionMapping.put(2, false);
//        joystickInversionMapping.put(3, true);

        var joystickButtonMapping = new HashMap<Integer, JoystickButtonFunctions>();
        //XBOX controller Wojtek
        joystickButtonMapping.put(6,JoystickButtonFunctions.nextCamera);
        joystickButtonMapping.put(7,JoystickButtonFunctions.prevCamera);
        joystickButtonMapping.put(3,JoystickButtonFunctions.angleMode);
        joystickButtonMapping.put(1,JoystickButtonFunctions.acroMode);
        joystickButtonMapping.put(4,JoystickButtonFunctions.posMode);
        joystickButtonMapping.put(0,JoystickButtonFunctions.noneMode);

        var axisActionsMapping = new HashMap<Integer, Actions>();
        //XBOX controller Wotjek
        //axisActionsMapping.put(4, Actions.shot);
        // XBOX controller Igor
        axisActionsMapping.put(2, Actions.shot);


        configuration.joystickMapping = joystickMapping;
        configuration.joystickInversionMapping = joystickInversionMapping;
        configuration.joystickButtonsMapping = joystickButtonMapping;
        configuration.axisActionsMapping = axisActionsMapping;
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
        String phongVertexShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/phongShader.vert")).getFile();
        String phongFragmentShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/phongShader.frag")).getFile();
        objectShader = new Shader(phongVertexShaderSource, phongFragmentShaderSource);
        objectShader.use();
        objectShader.setVec3("backgroundColor", DAY_COLOR);
        setUpLights(objectShader);

        String lightSourceVertexShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/lightSourceShader.vert")).getFile();
        String lightSourceFragmentShaderSource = Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource("shaders/lightSourceShader.frag")).getFile();
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

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            Matrix4f view = camera.getViewMatrix();
            Matrix4f projection = new Matrix4f().perspective(toRadians(camera.getFov()), (float) windowWidth / windowHeight, 0.1f, 1000f);

            inputHandler.processInput(window);

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
                droneStatusesMutex.lock();
                if(droneModels.size() != droneStatuses.map.size()) {
                    var newDroneModels = new ArrayList<Model>();
                    while(newDroneModels.size() != droneStatuses.map.size()) {
                        Model droneModel = createDroneModel();
                        newDroneModels.add(droneModel);
                    }
                    droneModels = newDroneModels;
                }
                var droneIterator = droneModels.iterator();
                droneStatuses.map.forEach((id, status) -> {
                    var model = droneIterator.next();
                    model.setPosition(status.position);
                    model.setRotation(status.rotation);
                });
                changeCamera();
                droneStatusesMutex.unlock();
                // END Drone models drawing
                // BEGIN Projectiles models drawing
                for(Model projectile: projectileModels) {
                    projectile.draw(stack, objectShader);
                }
                projectileStatusesMutex.lock();

                if(projectileModels.size() != projectileStatuses.map.size()) {
                    var newProjectileModels = new ArrayList<Model>();
                    while(newProjectileModels.size() != projectileStatuses.map.size()) {
                        Model projectileModel = createProjectileModel();
                        newProjectileModels.add(projectileModel);
                    }
                    projectileModels = newProjectileModels;
                }

                var projectileIterator = projectileModels.iterator();
                projectileStatuses.map.forEach((id, status) -> {
                    var model = projectileIterator.next();
                    model.setPosition(status.position);
                });
                projectileStatusesMutex.unlock();
                // END Projectiles models drawing

                axisModel.setPosition(new Vector3f(0,0,1));
                axisModel.draw(stack, objectShader);
                //busterModel.draw(stack, configuration.shader);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void changeCamera() {
        Vector3f dronePosition, droneRotation;
        if(droneStatuses.map.containsKey(controlledDrone.id)) {
            dronePosition = droneStatuses.map.get(controlledDrone.id).position;
            droneRotation = droneStatuses.map.get(controlledDrone.id).rotation;
        }
        else {
            dronePosition = new Vector3f();
            droneRotation = new Vector3f();
        }
        float currTime = (float) glfwGetTime();
        deltaTime = currTime - lastTime;
        lastTime = currTime;
        switch(configuration.type) {
            case DroneCamera -> {
                var cameraOffset = new Vector3f(-3.0f,0,-1.5f);
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                camera.setCameraPos(cameraPos);
                camera.setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                camera.setCameraUp(new Vector3f(0,0,-1));
            }
            case FreeCamera -> {
                camera.setCameraUp(new Vector3f(0,0,-1));
                camera.processInput(window, deltaTime);
            }
            case RacingCamera -> {
                var rot = new Vector3f(droneRotation);
                var cameraOffset = new Vector3f(-3,0,-1.5f).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                camera.setCameraPos(cameraPos);
                camera.setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                camera.setCameraUp(new Vector3f(0,0,-1).rotate(Convert.toQuaternion(rot)));
            }
            case HorizontalCamera -> {
                var rot = new Vector3f(droneRotation);
                rot.x = 0;
                rot.y = 0;
                var cameraOffset = new Vector3f(-3,0,-1.5f).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                camera.setCameraPos(cameraPos);
                camera.setCameraFront(new Vector3f(dronePosition).sub(cameraPos).normalize());
                camera.setCameraUp(new Vector3f(0,0,-1));
            }
            case ObserverCamera -> {
                var observerPos = new Vector3f(1f,1f,1f);
                camera.setCameraPos(observerPos);
                camera.setCameraFront(new Vector3f(dronePosition).sub(observerPos).normalize());
                camera.setCameraUp(new Vector3f(0,0,-1));
            }
            //TODO: SoftFPV is not implemented yet, now its copy of hardFPV
            case HardFPV, SoftFPV  -> {
                var rot = new Vector3f(droneRotation);
                var cameraOffset = new Vector3f(1.0f,0f,0.1f).rotate(Convert.toQuaternion(rot));
                var cameraPos = new Vector3f(dronePosition).add(cameraOffset);
                camera.setCameraPos(cameraPos);
                camera.setCameraFront(cameraOffset.normalize());
                camera.setCameraUp(new Vector3f(0,0,-1).rotate(Convert.toQuaternion(rot)));
            }
        }
    }

    @Override
    public void close() {
        droneStatusConsumer.stop();
        projectileStatusesConsumer.stop();
    }
}
