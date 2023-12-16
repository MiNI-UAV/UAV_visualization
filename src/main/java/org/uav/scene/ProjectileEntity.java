package org.uav.scene;

import lombok.AllArgsConstructor;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.model.Model;
import org.uav.model.SimulationState;
import org.uav.model.status.ProjectileStatus;
import org.uav.scene.shader.Shader;

import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
public class ProjectileEntity {
    private static final String DEFAULT_PROJECTILE_MODEL = "defaultProjectile";
    private final SimulationState simulationState;
    private final Map<String, Model> projectileModels;

    public void draw(MemoryStack stack, Shader shader, Collection<ProjectileStatus> projectileStatuses) {
        for(ProjectileStatus status: projectileStatuses) {
            if(!simulationState.getNotifications().projectileModelsNames.containsKey(status.id)) continue;
            String projectileModelName = simulationState.getNotifications().projectileModelsNames.get(status.id);
            Model projectileModel = projectileModels.getOrDefault(projectileModelName, projectileModels.get(DEFAULT_PROJECTILE_MODEL));

            projectileModel.setPosition(status.position);

            Vector3f rot = new Vector3f(1f, 0, 0).cross(status.velocity);
            float w = status.velocity.length() + new Vector3f(1f,0,0).dot(status.velocity);
            projectileModel.setRotation(new Quaternionf(rot.x, rot.y, rot.z, w).normalize());
            projectileModel.draw(stack, shader);
        }
    }
}
