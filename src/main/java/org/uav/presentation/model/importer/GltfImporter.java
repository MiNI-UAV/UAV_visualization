package org.uav.presentation.model.importer;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import org.javatuples.Pair;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.uav.logic.config.Config;
import org.uav.presentation.model.*;
import org.uav.presentation.model.animation.Animation;
import org.uav.presentation.model.animation.AnimationPlayer;
import org.uav.presentation.view.LoadingScreen;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.ByteBuffer.allocateDirect;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL21.GL_SRGB;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.uav.utils.IOUtils.extractImageData;

public class GltfImporter {

    public static final String UNSUPPORTED_ANIMATION_MODEL = "Unsupported animation model";
    private final Map<String, Texture> loadedTextures;
    private String textureDirectory;
    private final LoadingScreen loadingScreen;
    private final Config config;
    private Map<String, AnimationModel.Sampler> translationAnimationSamplers;
    private Map<String, AnimationModel.Sampler> rotationAnimationSamplers;
    private Map<String, AnimationModel.Sampler> scaleAnimationSamplers;
    private ArrayList<Model.AnimationInfo> animationInfos;

    public GltfImporter(LoadingScreen loadingScreen, Config config){
        loadedTextures = new HashMap<>();
        this.config = config;
        this.loadingScreen = loadingScreen;
    }

    public Model loadModel(String resourceFile, String textureDir) throws IOException {
        textureDirectory = textureDir;
        GltfModelReader reader = new GltfModelReader();
        GltfModel model = reader.read(Paths.get(resourceFile).toUri());

        SceneModel sceneModel = model.getSceneModels().get(0);

        animationInfos = new ArrayList<>();
        translationAnimationSamplers = new HashMap<>();
        rotationAnimationSamplers = new HashMap<>();
        scaleAnimationSamplers = new HashMap<>();
        if(!model.getAnimationModels().isEmpty()) {
            model.getAnimationModels().forEach(animationModel ->
                animationModel.getChannels().forEach(channel -> {
                    switch (channel.getPath()) {
                        case "translation" ->
                                translationAnimationSamplers.put(channel.getNodeModel().getName(), channel.getSampler());
                        case "rotation" ->
                                rotationAnimationSamplers.put(channel.getNodeModel().getName(), channel.getSampler());
                        case "scale" ->
                                scaleAnimationSamplers.put(channel.getNodeModel().getName(), channel.getSampler());
                        case "weights" -> {
                        }
                        default -> throw new RuntimeException(UNSUPPORTED_ANIMATION_MODEL);
                    }
                })
            );
        }

        List<ModelNode> children = new ArrayList<>();

        for ( NodeModel nodeModel: sceneModel.getNodeModels())
        {
            children.add(getChildModelNode(nodeModel));
        }

        return new Model(
                new ModelNode("OpenGLRootNode",
                        Collections.emptyList(),
                        children,
                        new Vector3f(),
                        new Quaternionf(0,0,0,1),
                        new Vector3f(1f),
                        new AnimationPlayer()),
                animationInfos
        );
    }

    private ModelNode getChildModelNode(NodeModel nodeModel) throws IOException {

        var translationAnimation = getAnimation(nodeModel, translationAnimationSamplers);
        var rotationAnimation = getRotationAnimation(nodeModel, rotationAnimationSamplers);
        var scaleAnimation = getAnimation(nodeModel, scaleAnimationSamplers);
        addAnimationInfo(nodeModel.getName(), "Hover", translationAnimation, "translation");
        addAnimationInfo(nodeModel.getName(), "Hover", rotationAnimation, "rotation");
        addAnimationInfo(nodeModel.getName(), "Hover", scaleAnimation, "scale");
        var animationPlayer = new AnimationPlayer();
        if(!translationAnimation.isEmpty() || !rotationAnimation.isEmpty() || !scaleAnimation.isEmpty()) {
            animationPlayer.put(
                    "Hover",
                    new Animation(translationAnimation, rotationAnimation, scaleAnimation)
            );
        }

        List<Mesh> meshes = processMeshModels(nodeModel.getMeshModels());

        List<ModelNode> children = new ArrayList<>();
        for(NodeModel childrenNode : nodeModel.getChildren()) {
            children.add(getChildModelNode(childrenNode));
        }
        if(nodeModel.getMatrix() != null) // TODO: Matrices  get priority
        {
            float[] m = nodeModel.getMatrix();
            Vector3f localTranslation = new Vector3f(new float[]{m[12],m[13],m[14]});
            Vector3f localScale = new Vector3f(new float[]{0,0,0});
            Quaternionf localRotation = new Quaternionf();
            return new ModelNode(
                    nodeModel.getName(),
                    meshes,
                    children,
                    localTranslation,
                    localRotation,
                    localScale,
                    animationPlayer
            );
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

        return new ModelNode(
                nodeModel.getName(),
                meshes,
                children,
                localTranslation,
                localRotation,
                localScale,
                animationPlayer
        );
    }

    private <T> void addAnimationInfo(String modelName, String animationName, List<Pair<Float, T>> animation, String type) {
        if(animation.isEmpty()) return;
        animationInfos.add(
                new Model.AnimationInfo(
                        animationName, type, modelName, animation.get(animation.size()-1).getValue0() - animation.get(0).getValue0(), 0
                )
        );
    }

    private List<Pair<Float, Vector3f>> getAnimation(NodeModel nodeModel, Map<String, AnimationModel.Sampler> samplerMap) throws IOException {
        if(!samplerMap.containsKey(nodeModel.getName())) return new ArrayList<>();
        var sampler = samplerMap.get(nodeModel.getName());
        if(sampler.getInterpolation() != AnimationModel.Interpolation.LINEAR) throw new IOException(UNSUPPORTED_ANIMATION_MODEL + " " + sampler.getInterpolation());
        AccessorFloatData animationTimeAccessor = (AccessorFloatData) sampler.getInput().getAccessorData();
        AccessorFloatData transformationAccessor = (AccessorFloatData) sampler.getOutput().getAccessorData();
        List<Pair<Float, Vector3f>> animation = new ArrayList<>();
        int n = animationTimeAccessor.getNumElements();
        for (int i = 0; i < n; i++) {
            float t = animationTimeAccessor.get(i, 0);
            float x = transformationAccessor.get(i, 0);
            float y = transformationAccessor.get(i, 1);
            float z = transformationAccessor.get(i, 2);
            animation.add(new Pair<>(t, new Vector3f(x, y, z)));
        }
        return animation;
    }

    private List<Pair<Float, Quaternionf>> getRotationAnimation(NodeModel nodeModel, Map<String, AnimationModel.Sampler> samplerMap) throws IOException {
        if(!samplerMap.containsKey(nodeModel.getName())) return new ArrayList<>();
        var sampler = samplerMap.get(nodeModel.getName());
        if(sampler.getInterpolation() != AnimationModel.Interpolation.LINEAR) throw new IOException(UNSUPPORTED_ANIMATION_MODEL);
        AccessorFloatData animationTimeAccessor = (AccessorFloatData) sampler.getInput().getAccessorData();
        AccessorFloatData transformationAccessor = (AccessorFloatData) sampler.getOutput().getAccessorData();
        List<Pair<Float, Quaternionf>> animation = new ArrayList<>();
        int n = animationTimeAccessor.getNumElements();
        for (int i = 0; i < n; i++) {
            float t = animationTimeAccessor.get(i, 0);
            float x = transformationAccessor.get(i, 0);
            float y = transformationAccessor.get(i, 1);
            float z = transformationAccessor.get(i, 2);
            float w = transformationAccessor.get(i, 3);
            animation.add(new Pair<>(t, new Quaternionf(x, y, z, w)));
        }
        return animation;
    }

    private List<Mesh> processMeshModels(List<MeshModel> meshModels) {
        List<Mesh> meshes = new ArrayList<>();
        for (MeshModel meshModel : meshModels)
        {
            for (MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels())
            {
                List<ModelVertex> ver = new ArrayList<>();
                List<Vector3f> positions = getPosition(meshPrimitiveModel);
                List<Vector3f> normals = getNormals(meshPrimitiveModel);
                List<Vector2f> textureCoords = getTextureCoords(meshPrimitiveModel);
                for(int i=0; i< positions.size(); i++) {
                    Vector3f pos = positions.get(i);
                    Vector3f nor = i < normals.size() ? normals.get(i) : new Vector3f();
                    Vector2f texc = i < textureCoords.size() ? textureCoords.get(i) : new Vector2f();
                    ver.add(new ModelVertex(pos, nor, texc));
                }

                List<Integer> ind = getIndices(meshPrimitiveModel);

                MaterialModelV2 materialModel = (MaterialModelV2) meshPrimitiveModel.getMaterialModel();
                Material material = getMaterial(materialModel);

                Texture albedoTexture = null;
                Texture normalTexture = null;
                Texture metallicRoughnessTexture = null;
                Texture ambientOcclusionTexture = null;

                if(materialModel != null && materialModel.getBaseColorTexture() != null) albedoTexture = loadTexture(materialModel.getBaseColorTexture(), false);
                if(materialModel != null && materialModel.getNormalTexture() != null) normalTexture = loadTexture(materialModel.getNormalTexture(), true);
                if(materialModel != null && materialModel.getMetallicRoughnessTexture() != null) metallicRoughnessTexture = loadTexture(materialModel.getMetallicRoughnessTexture(), true);
                if(materialModel != null && materialModel.getOcclusionTexture() != null) ambientOcclusionTexture = loadTexture(materialModel.getOcclusionTexture(), true);
                boolean isTransparent = albedoTexture != null && albedoTexture.isTransparent();

                var list = new ArrayList<Texture>();
                if(albedoTexture != null) list.add(albedoTexture);
                meshes.add(new Mesh(ver, ind, list, albedoTexture, normalTexture, metallicRoughnessTexture, ambientOcclusionTexture,isTransparent, material));
            }
        }
        return meshes;
    }

    private static Material getMaterial(MaterialModelV2 materialModel) {
        if(materialModel == null)
            return new Material(new Vector4f(1,1,1,1), 1,1,1,1);
        Vector4f albedo = new Vector4f(materialModel.getBaseColorFactor());
        float normalScale = materialModel.getNormalScale();
        float roughnessFactor = materialModel.getRoughnessFactor();
        float metallicFactor = materialModel.getMetallicFactor();
        float aoStrength = materialModel.getOcclusionStrength();

        return new Material(albedo, normalScale, roughnessFactor, metallicFactor, aoStrength);
    }

    private Texture loadTexture(TextureModel textureModel, boolean forceLinearRgb) {
        if(loadedTextures.containsKey(textureModel.getImageModel().getUri()))
            return loadedTextures.get(textureModel.getImageModel().getUri());

        ImageModel imageModel = textureModel.getImageModel();
        String s = imageModel.getUri();
        String fileName = Paths.get(s).getFileName().toString();
        return loadTexture(textureModel.getImageModel().getUri(), Paths.get(textureDirectory, fileName), forceLinearRgb);
    }

    private Texture loadTexture(String name, Path path, boolean forceLinearRgb) {
        loadingScreen.render("Loading " + name + "...");

        try {
            BufferedImage img = ImageIO.read(new File(path.toString()));
            ByteBuffer imageDirectByteBuffer = allocateDirect(img.getHeight() * img.getWidth() * img.getColorModel().getNumComponents());
            imageDirectByteBuffer.put(ByteBuffer.wrap(extractImageData(img)));
            imageDirectByteBuffer.position(0);
            int components = img.getColorModel().getNumComponents();
            int format = switch(components) {
                case 1 -> GL_BACK;
                case 4 -> GL_RGBA;
                default -> GL_RGB;
            };
            ColorSpace colorSpace = img.getColorModel().getColorSpace();
            int internalFormat = !forceLinearRgb && config.getGraphicsSettings().getUseGammaCorrection() ?
                    switch(components) {
                        case 1 -> GL_BACK;
                        case 4 -> colorSpace.isCS_sRGB()? GL_SRGB_ALPHA: GL_RGBA;
                        default -> colorSpace.isCS_sRGB()? GL_SRGB: GL_RGB;
                    } : format;
            boolean transparent = Arrays.stream(
                    img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth())
            ).anyMatch((rgb -> ((rgb >> 24) & 0xFF) != 0xFF));

            int texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, img.getWidth(), img.getHeight(), 0, format, GL_UNSIGNED_BYTE, imageDirectByteBuffer);
            glGenerateMipmap(GL_TEXTURE_2D);

            Texture texture1 = new Texture(texture, "texture_diffuse", transparent);
            loadedTextures.put(name, texture1);
            return texture1;
        } catch(IOException e) {
            throw new RuntimeException("Could not load a texture: " + name);
        }
    }

    private static List<Integer> getIndices(MeshPrimitiveModel meshPrimitiveModel) {
        List<Integer> list = new ArrayList<>();
        AccessorModel accessorModel = meshPrimitiveModel.getIndices();
        if(accessorModel == null) return List.of();
        AccessorData accessorData = accessorModel.getAccessorData();
        if(accessorData.getComponentType() == short.class)
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
        AccessorModel accessorModel = meshPrimitiveModel.getAttributes().get("POSITION");
        return getVector3fs(accessorModel);
    }

    private static List<Vector3f> getNormals(MeshPrimitiveModel meshPrimitiveModel) {
        AccessorModel accessorModel = meshPrimitiveModel.getAttributes().get("NORMAL");
        return getVector3fs(accessorModel);
    }

    private static List<Vector3f> getVector3fs(AccessorModel accessorModel) {
        var list = new ArrayList<Vector3f>();
        AccessorData accessorData = accessorModel.getAccessorData();
        AccessorFloatData accessorFloatData = (AccessorFloatData) accessorData;
        int n = accessorFloatData.getNumElements();
        for (int i = 0; i < n; i++)
        {
            float x = accessorFloatData.get(i, 0);
            float y = accessorFloatData.get(i, 1);
            float z = accessorFloatData.get(i, 2);
            list.add(new Vector3f(x, y, z));
        }
        return list;
    }

    private static List<Vector2f> getTextureCoords(MeshPrimitiveModel meshPrimitiveModel) {
        var list = new ArrayList<Vector2f>();
        AccessorModel accessorModel = meshPrimitiveModel.getAttributes().get("TEXCOORD_0");
        if(accessorModel == null)
            return list;
        AccessorData accessorData = accessorModel.getAccessorData();
        AccessorFloatData accessorFloatData =
                (AccessorFloatData) accessorData;
        int n = accessorFloatData.getNumElements();
        for (int i = 0; i < n; i++)
        {
            float x = accessorFloatData.get(i, 0);
            float y = accessorFloatData.get(i, 1);
            list.add(new Vector2f(x, y));
        }
        return list;
    }
}
