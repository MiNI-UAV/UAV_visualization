package org.uav.presentation.entity.bulletTrail;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.UavVisualization;
import org.uav.logic.state.projectile.ProjectileStatus;
import org.uav.presentation.rendering.Shader;

import java.io.IOException;
import java.nio.FloatBuffer;
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

    public void draw(FloatBuffer viewBuffer, FloatBuffer projectionBuffer, Collection<ProjectileStatus> projectiles) {
        bulletTrailShader.use();
        bulletTrailShader.setMatrix4f("view", viewBuffer);
        bulletTrailShader.setMatrix4f("projection", projectionBuffer);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            HashMap<Integer, BulletTrail> newBulletTrails = new HashMap<>();
            for(ProjectileStatus status: projectiles) {
                BulletTrail bt;
                if(bulletTrails.containsKey(status.id)) {
                    bt = bulletTrails.get(status.id);
                } else {
                    bt = new BulletTrail(bulletTrailShader);
                }
                bt.addPoint(status.position);
                newBulletTrails.put(status.id, bt);
                bt.draw(stack);
            }
            bulletTrails = newBulletTrails;
        }
    }
}
