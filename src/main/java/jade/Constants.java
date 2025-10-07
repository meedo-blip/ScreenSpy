package jade;

import font.MyFont;
import org.joml.Vector4f;
import renderer.Shader;
import util.AssetPool;
import util.Utils;

public class Constants {
    public static final int MAX_BATCH_SIZE = 1000;

    public static Shader DEFAULT_SH = AssetPool.getShader( "assets/shaders/default.glsl"),
                        FONT_SH = AssetPool.getShader("assets/shaders/sdf.glsl"),
                        CIRCLE_SH = AssetPool.getShader("assets/shaders/circle.glsl");

    public static final MyFont ARIAL_FONT = new MyFont("assets\\fonts\\arialbd.ttf", 64);

    public static final Vector4f BLACK = new Vector4f(0,0,0,1),
    WHITE = new Vector4f(1,1,1,1),
    INVISIBLE = new Vector4f(1,1,1,0),
    YELLOW_ORANGE = Utils.hexToRgba("ff9900"),
    YELLOW = new Vector4f(1,1,0,1),
    BROWN = Utils.hexToRgba("663300"),
    RED = new Vector4f(1,0,0,1);
}
