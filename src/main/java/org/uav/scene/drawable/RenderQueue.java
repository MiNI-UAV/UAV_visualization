package org.uav.scene.drawable;

import org.javatuples.Pair;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.model.Mesh;
import org.uav.scene.shader.Shader;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class RenderQueue {

    private final Queue<Pair<Mesh, Matrix4f>> transparentMeshes;
    private final Queue<Pair<Mesh, Matrix4f>> standardMeshes;

    public RenderQueue(Vector3f cameraPosition) {
        Comparator<Pair<Mesh, Matrix4f>> comparator = (Pair<Mesh, Matrix4f> p1, Pair<Mesh, Matrix4f> p2) -> {
            Vector3f distance1 = p1.getValue1().getTranslation(new Vector3f()).sub(cameraPosition);
            Vector3f distance2 = p2.getValue1().getTranslation(new Vector3f()).sub(cameraPosition);
            return Float.compare(distance1.length(), distance2.length());
        };
        transparentMeshes = new PriorityQueue<>(comparator);
        standardMeshes = new ArrayDeque<>();
    }

    public void addMesh(Mesh mesh, Matrix4f modelMatrix) {
        if(mesh.isTransparent())
            transparentMeshes.add(Pair.with(mesh, modelMatrix));
        else
            standardMeshes.add(Pair.with(mesh, modelMatrix));
    }

    public void render(MemoryStack stack, Shader shader) {
        while(!standardMeshes.isEmpty()) {
            var meshPair = standardMeshes.remove();
            meshPair.getValue0().draw(stack, shader, meshPair.getValue1());
        }
        while(!transparentMeshes.isEmpty()) {
            var meshPair = transparentMeshes.remove();
            meshPair.getValue0().draw(stack, shader, meshPair.getValue1());
        }

    }
}
