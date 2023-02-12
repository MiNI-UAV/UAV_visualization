package org.opengl.drawable;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.opengl.model.Model;
import org.opengl.shader.Shader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawableStars implements Drawable {

    private final Random random;
    private final Model objectModel;
    private final int starCount;
    private final int shaderOffset;
    public final List<Vector3f> lightPositions;


    public DrawableStars(Model model, int starCount, int shaderOffset) {
        random = new Random();
        objectModel = model;
        this.starCount = starCount;
        this.shaderOffset = shaderOffset;
        lightPositions = new ArrayList<>();

        generateStars();
    }

    @Override
    public void draw(MemoryStack stack, Shader shader) {
        shader.use();
        shader.setVec3("lightColor", new Vector3f(1.f, 1.f, 1.f));
        for(int i=1; i< starCount; i++) {
            var model = new Matrix4f()
                    .translate(getPosition(i))
                    .scale(.05f,.05f,.05f);
            shader.setMatrix4f(stack,"model", model);
            objectModel.draw(shader);
        }
    }

    public Vector3f getPosition(int i) {
        return lightPositions.get(i);
    }
    @Override
    public Vector3f getPosition()  {
        return new Vector3f(0.f);
    }


    private void generateStars() {
        for(int i=0; i < starCount; i++) {
            lightPositions.add(new Vector3f(
                    random.nextFloat() * 20 - 10,
                    random.nextFloat() * 20 - 10,
                    random.nextFloat() * 100 - 60
            ));
        }
    }

    public void moveStars(Shader shader) {
        for(int i=0; i < starCount; i++) {
            Vector3f old = lightPositions.get(i);
            Vector3f v = (old.z > 40)?
                    new Vector3f(random.nextFloat()*20 - 10, random.nextFloat()*20 - 10, -60f):
                    new Vector3f(old.x, old.y, old.z + 0.4f);
            lightPositions.set(i, v);
            shader.setVec3("pointLights[" + (i + shaderOffset) + "].position", v);
        }
    }
}
