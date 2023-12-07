package org.uav.scene;

import org.javatuples.Triplet;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.uav.model.Mesh;
import org.uav.scene.shader.Shader;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class OrderedRenderQueue {

    private final Queue<Triplet<Mesh, Matrix4f, Shader>> transparentMeshes;
    private final Queue<Triplet<Mesh, Matrix4f, Shader>> standardMeshes;

    public OrderedRenderQueue(Vector3f cameraPosition) {
        Comparator<Triplet<Mesh, Matrix4f, Shader>> comparator =
                (Triplet<Mesh, Matrix4f, Shader> p1, Triplet<Mesh, Matrix4f, Shader> p2) -> {
            Vector3f distance1 = p1.getValue1().getTranslation(new Vector3f()).sub(cameraPosition);
            Vector3f distance2 = p2.getValue1().getTranslation(new Vector3f()).sub(cameraPosition);
            return Float.compare(distance1.length(), distance2.length());
        };
        transparentMeshes = new PriorityQueue<>(comparator);
        standardMeshes = new ArrayDeque<>();
    }

    public void addMesh(Mesh mesh, Matrix4f modelMatrix, Shader shader) {
        if(mesh.isTransparent())
            transparentMeshes.add(Triplet.with(mesh, modelMatrix, shader));
        else
            standardMeshes.add(Triplet.with(mesh, modelMatrix, shader));
    }

    public void render(MemoryStack stack) {
        while(!standardMeshes.isEmpty()) {
            var drawable = standardMeshes.remove();
            drawable.getValue0().draw(stack, drawable.getValue2(), drawable.getValue1());
        }
        while(!transparentMeshes.isEmpty()) {
            var drawable = transparentMeshes.remove();
            drawable.getValue0().draw(stack, drawable.getValue2(), drawable.getValue1());
        }

    }
}
