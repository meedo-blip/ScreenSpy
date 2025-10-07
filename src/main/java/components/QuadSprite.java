package components;

import jade.Constants;
import org.joml.*;
import util.Utils;

public abstract class QuadSprite extends Sprite {
    public int texId = -1;

    protected Vector2f offsetPos;

    protected float[] texCoords = {
            1f, 1f, // 1. Top right
            0f, 1f, // 2. Bottom right
            0f, 0f, // 3. Bottom left
            1f, 0f  // 4. Top left
    };

    public int getTexId() {
        return texId;
    }

    @Override
    public void start() {
        super.start();
        if(color == null)
            color = Constants.INVISIBLE;
    }

    public float[] getTexCoords() {
        return texCoords;
    }

    public boolean checkCollision(Sprite other) {
        return Utils.checkCollision(transform, other.transform);
    }
}
