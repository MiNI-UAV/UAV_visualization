package org.opengl.importer;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.GltfModelV2;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.opengl.OpenGLScene;
import org.opengl.model.*;
import org.opengl.model.Model;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class GltfImporter {

    private static List<TextureModel> textureModels;
    private static Map<String, Texture> loadedTextures;
    private static String textureDirectory;

    public static Model loadModel(String resourceFile, String textureDir) throws URISyntaxException, IOException {
        textureDirectory = textureDir;
        String path = "file://" + Objects.requireNonNull(OpenGLScene.class.getClassLoader().getResource(resourceFile)).getFile();
        GltfModelReader reader = new GltfModelReader();
        GltfModelV2 model = (GltfModelV2) reader.read(new URI(path));
        textureModels = model.getTextureModels();
        loadedTextures = new HashMap<>();

        SceneModel sceneModel = model.getSceneModels().get(0);

        List<ModelNode> children = new ArrayList<>();

        for ( NodeModel nodeModel: sceneModel.getNodeModels())
        {
            children.add(getChildModelNode(nodeModel));
        }

        // TODO: glTF defines +Y as up, +Z as forward, and -X as right
        return new Model(
                new ModelNode("RootNode",
                        Collections.emptyList(),
                        children,
                        new Vector3f(),
                        new Quaternionf(-0.7071f,0f,0, 0.7071f),
                        new Vector3f(1f)
                )
        );
    }

    private static ModelNode getChildModelNode(NodeModel nodeModel) {

        List<Mesh> meshes = processMeshModels(nodeModel.getMeshModels());

        List<ModelNode> children = new ArrayList<>();
        for(NodeModel childrenNode : nodeModel.getChildren()) {
            children.add(getChildModelNode(childrenNode));
        }
        Matrix4f localTransform = floatToMatrix4f(nodeModel.computeLocalTransform(null));
        if(nodeModel.getMatrix() != null) // TODO: Matrices  get priority. Check if that's true on asset basis
        {
            float[] m = nodeModel.getMatrix();
            Vector3f localTranslation = new Vector3f(new float[]{m[12],m[13],m[14]});
            Vector3f localScale = new Vector3f(new float[]{0,0,0}); // TODO
            Quaternionf localRotation = new Quaternionf(); // TODO
            //return new ModelNode0(nodeModel.getName(), meshes, children, new Vector3f(localTranslation), localRotation, new Vector3f(localScale), localTransform);
            return new ModelNode(nodeModel.getName(), meshes, children, localTranslation , localRotation, localScale);
        }
        float[] translation = nodeModel.getTranslation() != null ? nodeModel.getTranslation(): new float[]{0, 0, 0};
        Vector3f localTranslation = new Vector3f(translation);
        float[] rotation = nodeModel.getRotation() != null ? nodeModel.getRotation(): new float[]{0, 0, 0, 1};
        float x = rotation[0];
        float y = rotation[1];
        float z = rotation[2];
        float w = rotation[3];
        Quaternionf localRotation = new Quaternionf(x, y, z, w);
        float[] scale = nodeModel.getScale() != null ? nodeModel.getScale(): new float[]{1, 1, 1};
        Vector3f localScale = new Vector3f(scale);

        Matrix4f localTransformation = new Matrix4f()
                .translate(localTranslation)
                .rotate(localRotation)
                .scale(localScale);
        float[] res = new float[16];
        nodeModel.computeLocalTransform(res);
        // TODO: nie zgadza się

        //return new ModelNode(nodeModel.getName(), meshes, children, localTranslation, localRotation, localScale, localTransform);
        return new ModelNode(nodeModel.getName(), meshes, children, localTranslation, localRotation, localScale);
    }

    private static Matrix4f floatToMatrix4f(float[] f) {
        return new Matrix4f(
                f[0], f[1], f[2], f[3],
                f[4], f[5], f[6], f[7],
                f[8], f[9], f[10], f[11],
                f[12], f[13], f[14], f[15]
        );
    }

    private static  List<Mesh> processMeshModels(List<MeshModel> meshModels) {
        List<Mesh> meshes = new ArrayList<>();
        for (MeshModel meshModel : meshModels)
        {
            for (MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels())
            {
                List<Vertex> vertices = new ArrayList<>();
                List<Vector3f> pos = getPosition(meshPrimitiveModel);
                List<Vector3f> nor = getNormals(meshPrimitiveModel);
                List<Vector2f> texc = getTextureCoords(meshPrimitiveModel);
                for(int i=0; i< pos.size(); i++)
                    vertices.add(new Vertex(pos.get(i), nor.get(i), texc.get(i)));

                List<Integer> ind = getIndices(meshPrimitiveModel);

                MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();

                //float[] bcf = (float[]) materialModel.getValues().get("baseColorFactor");
                Material material = new Material(
                        new Vector3f(0.5f,0.5f,0.5f),
                        0.1f,//(float) materialModel.getValues().get("roughnessFactor"),
                        0.5f//(float) materialModel.getValues().get("metallicFactor")
                        );


                /*if(!materialModel.getValues().containsKey("baseColorTexture")) {
                    meshes.add(new Mesh(vertices, ind, List.of(), material));
                    continue;
                }*/
                TextureModel textureModel = textureModels.get((Integer) materialModel.getValues().get("baseColorTexture")); // TODO Crash null when no texture on model
                Texture texture = loadTexture(textureModel);
                meshes.add(new Mesh(vertices, ind, List.of(texture), material));
            }
        }
        return meshes;
    }

    private static Texture loadTexture(TextureModel textureModel) {
        if(loadedTextures.containsKey(textureModel.getImageModel().getUri()))
            return loadedTextures.get(textureModel.getImageModel().getUri());

        ImageModel imageModel = textureModel.getImageModel();
        String s = imageModel.getUri();
        String fileName = s.substring(s.lastIndexOf('/') + 1);
        String path = textureDirectory + "/" + fileName;
        int[] w = new int[1];
        int[] h = new int[1];
        int[] components = new int[1];
        System.out.println("loading " + path);
        String imagePath = new File(OpenGLScene.class.getClassLoader().getResource(path).getPath()).toString();
        ByteBuffer image = stbi_load(imagePath, w, h, components, 0);
        int format = GL_RGB;
        if(components[0] == 4) format = GL_RGBA;
        if(components[0] == 1) format = GL_BACK;
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w[0], h[0], 0, format, GL_UNSIGNED_BYTE, image);
        glGenerateMipmap(GL_TEXTURE_2D);
        stbi_image_free(image);

        Texture texture1 = new Texture(texture, "texture_diffuse");
        loadedTextures.put(textureModel.getImageModel().getUri(), texture1);
        return texture1;
    }

    private static List<Integer> getIndices(MeshPrimitiveModel meshPrimitiveModel) {
        List<Integer> list = new ArrayList<>();
        AccessorModel accessorModel = meshPrimitiveModel.getIndices();
        AccessorData accessorData = accessorModel.getAccessorData();
        if(accessorData.getComponentType() == short.class) // TODO: prettier
        {
            AccessorShortData accessorShortData =
                    (AccessorShortData) accessorData;
            int n = accessorShortData.getNumElements();
            for (int i = 0; i < n; i++) {
                int x = accessorShortData.get(i, 0);
                list.add(x);
            }
        }
        else if(accessorData.getComponentType() == int.class)
        {
            AccessorIntData accessorIntData =
                    (AccessorIntData) accessorData;
            int n = accessorIntData.getNumElements();
            for (int i = 0; i < n; i++) {
                int x = accessorIntData.get(i, 0);
                list.add(x);
            }
        }
        return list;
    }

    private static List<Vector3f> getPosition(MeshPrimitiveModel meshPrimitiveModel) {
        var list = new ArrayList<Vector3f>();
        AccessorModel accessorModel =
                meshPrimitiveModel.getAttributes().get("POSITION");
        AccessorData accessorData = accessorModel.getAccessorData();
        AccessorFloatData accessorFloatData =
                (AccessorFloatData) accessorData;
        int n = accessorFloatData.getNumElements();
        for (int i = 0; i < n; i++)
        {
            float x = accessorFloatData.get(i, 0);
            float y = accessorFloatData.get(i, 1);
            float z = accessorFloatData.get(i, 2);
            //System.out.println("Position " + i + " is " + x + " " + y + " " + z);
            list.add(new Vector3f(x, y, z));
        }
        return list;
    }

    private static List<Vector3f> getNormals(MeshPrimitiveModel meshPrimitiveModel) {
        var list = new ArrayList<Vector3f>();
        AccessorModel accessorModel =
                meshPrimitiveModel.getAttributes().get("NORMAL");
        AccessorData accessorData = accessorModel.getAccessorData();
        AccessorFloatData accessorFloatData =
                (AccessorFloatData) accessorData;
        int n = accessorFloatData.getNumElements();
        for (int i = 0; i < n; i++)
        {
            float x = accessorFloatData.get(i, 0);
            float y = accessorFloatData.get(i, 1);
            float z = accessorFloatData.get(i, 2);
            //System.out.println("Normal " + i + " is " + x + " " + y + " " + z);
            list.add(new Vector3f(x, y, z));
        }
        return list;
    }

    private static List<Vector2f> getTextureCoords(MeshPrimitiveModel meshPrimitiveModel) {
        var list = new ArrayList<Vector2f>();
        AccessorModel accessorModel =
                meshPrimitiveModel.getAttributes().get("TEXCOORD_0");
        AccessorData accessorData = accessorModel.getAccessorData();
        AccessorFloatData accessorFloatData =
                (AccessorFloatData) accessorData;
        int n = accessorFloatData.getNumElements();
        for (int i = 0; i < n; i++)
        {
            float x = accessorFloatData.get(i, 0);
            float y = accessorFloatData.get(i, 1);
            //System.out.println("TextureCoords " + i + " is " + x + " " + y );
            list.add(new Vector2f(x, y));
        }
        return list;
    }
}
