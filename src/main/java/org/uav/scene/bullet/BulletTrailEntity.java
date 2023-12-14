package org.uav.scene.bullet;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.model.SimulationState;
import org.uav.model.status.ProjectileStatus;
import org.uav.scene.shader.Shader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;


public class BulletTrailEntity {
    private HashMap<Integer, BulletTrail> bulletTrails;
    private final Shader bulletTrailShader;

    public BulletTrailEntity() throws IOException {
        bulletTrails = new HashMap<>();

        var bulletTrailVertexShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/bullets/bulletTrailShader.vert"));
        var bulletTrailGeometryShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/bullets/bulletTrailShader.geom"));
        var bulletTrailFragmentShaderSource = Objects.requireNonNull(UavVisualization.class.getClassLoader().getResourceAsStream("shaders/bullets/bulletTrailShader.frag"));
        bulletTrailShader = new Shader(bulletTrailVertexShaderSource, bulletTrailGeometryShaderSource, bulletTrailFragmentShaderSource);
        bulletTrailShader.use();
        bulletTrailShader.setVec3("color", new Vector3f(1,1,1));
        bulletTrailShader.setFloat("startingOpacity", 1f);
    }

    public void draw(MemoryStack stack, Matrix4f view, Matrix4f projection, Collection<ProjectileStatus> projectiles, SimulationState simulationState) {
        bulletTrailShader.use();
        bulletTrailShader.setMatrix4f(stack,"view", view);
        bulletTrailShader.setMatrix4f(stack,"projection", projection);

        HashMap<Integer, BulletTrail> newBulletTrails = new HashMap<>();
        for(ProjectileStatus status: projectiles) {
            BulletTrail bt;
            if(bulletTrails.containsKey(status.id)) {
                bt = bulletTrails.get(status.id);
            } else {
                bt = new BulletTrail(bulletTrailShader);
                simulationState.getPlayerDrone().ifPresent(drone-> bt.addPoint(drone.position));
            }
            bt.addPoint(status.position);
            newBulletTrails.put(status.id, bt);
            bt.draw(stack);
        }
        bulletTrails = newBulletTrails;
    }
}
