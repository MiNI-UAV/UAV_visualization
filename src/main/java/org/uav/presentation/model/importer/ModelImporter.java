package org.uav.presentation.model.importer;

import lombok.AllArgsConstructor;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.uav.presentation.model.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public class ModelImporter {
    private final GltfImporter gltfImporter;
    private final String assetsDirectory;


    public Map<String, Model> loadModelMap(String directoryName) throws IOException {
        Map<String, Model> map = new HashMap<>();
        var dirPath = Paths.get(assetsDirectory, directoryName).toString();
        File directory = new File(dirPath);
        for(File model: Objects.requireNonNull(directory.listFiles())) {
            var modelFile = Paths.get(model.getAbsolutePath(), "model", "model.gltf").toString();
            var textureDir = Paths.get(model.getAbsolutePath(), "textures").toString();
            var droneModel = gltfImporter.loadModel(modelFile, textureDir);
            map.put(model.getName(), droneModel);
        }
        return map;
    }

    public Model loadModel(String modelDir) throws IOException {
        var modelFile = Paths.get(assetsDirectory, modelDir, "model", "model.gltf").toString();
        var textureDir = Paths.get(assetsDirectory, modelDir, "textures").toString();
        var model = gltfImporter.loadModel(modelFile, textureDir);
        model.setPosition(new Vector3f());
        model.setRotation(new Quaternionf());
        return model;
    }
}
