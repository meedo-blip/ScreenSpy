package renderer;

import components.Sprite;

import java.util.List;

import static jade.Constants.MAX_BATCH_SIZE;

public abstract class RenderBatch {

    protected List<Sprite> sprites;
    protected int vertexSize;
    protected int numSprites = 0;
    protected float[] vertices;

    protected int vaoID, vboID;
    protected Shader shader;

    public abstract void start();

    public abstract boolean addSprite(Sprite spr);

    protected abstract void loadVertexProperties(int index);

    public abstract void render();

    protected boolean hasRoom() {
        return this.sprites.size() < MAX_BATCH_SIZE;
    }

    protected boolean usesShader(Shader shader) {
        return this.shader == shader;
    }

    public boolean removeSprite(Sprite spr) {
        if (this.sprites.remove(spr)) {
            numSprites--;
            for (int i = 0; i < sprites.size(); i++) {
                loadVertexProperties(i);
            }
            return true;
        }

        return false;
    }



}
