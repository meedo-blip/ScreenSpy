package components;

import jade.Constants;
import jade.Transform;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class FontSprite extends QuadSprite {

    public FontSprite(int texId, float[] texCoords, Vector4f color) {
        this.shader = Constants.FONT_SH;
        this.texId = texId;
        this.color = color;
        this.texCoords = texCoords;
    }

    @Override
    public void update(float dt) {
        // FontSprite does not need to update anything per frame
    }
}
