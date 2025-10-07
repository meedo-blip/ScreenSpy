package util;

import jade.Sound;
import renderer.BatchGen;
import renderer.DefaultBatch;
import renderer.RenderBatch;
import renderer.Shader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AssetPool {
    private static final Map<String, Shader> shaders = new HashMap<>();
    private static final Map<String, Integer> textures = new HashMap<>();
    private static final Map<String, Sound> sounds = new HashMap<>();


    private static final Map<Integer, BatchGen> shaderToBatch = new HashMap<>();

    public static Shader getShader(String filename) {
        File file = new File(filename);

        if (shaders.containsKey(file.getAbsolutePath())) {
            return shaders.get(file.getAbsolutePath());
        } else {
            Shader shader = new Shader(filename);
            shader.compile();
            shaders.put(file.getAbsolutePath(), shader);
            return shader;
        }
    }

    public static int getTexture(String resourceName) {
        File file = new File(resourceName);
        if (textures.containsKey(file.getAbsolutePath())) {
            return textures.get(file.getAbsolutePath());
        } else {
            int texture = Utils.createTexture(resourceName, true);
            textures.put(file.getAbsolutePath(), texture);
            return texture;
        }
    }


    public static Sound getSound(String resourceName, boolean loop) {
        File file = new File(resourceName);
        if (sounds.containsKey(file.getAbsolutePath())) {
            return sounds.get(file.getAbsolutePath());
        } else {
            Sound sound = new Sound(resourceName, loop);
            sounds.put(file.getAbsolutePath(), sound);
            return sound;
        }
    }

    public static RenderBatch getBatchOf(Shader shader) {
        return shaderToBatch.getOrDefault(shader.getId(), (BatchGen) () -> new DefaultBatch(shader)).genBatch();
    }

    public static void addBatchGetterToShader(Shader shader, BatchGen batchGetter) {
        shaderToBatch.put(shader.getId(), batchGetter);
    }


}
