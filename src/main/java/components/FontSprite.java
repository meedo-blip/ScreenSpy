package components;

import jade.Constants;
import org.joml.Vector4f;

public class FontSprite extends QuadSprite {

    public FontSprite(int texId, float[] texCoords, Vector4f color) {
        this.shader = Constants.FONT_SH;
        this.texId = texId;
        this.color = color;
        this.texCoords = texCoords;
    }

    public void start() {
        super.start();
    }

    @Override
    public void update(float dt) {
        transform.position.x = pForm.position.x + offsetForm.position.x;
        transform.position.y = pForm.position.y + offsetForm.position.y;
    }
}
