package renderer;

import components.QuadSprite;
import components.Sprite;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;


public class Renderer {

    private final List<RenderBatch> batches;

    public Renderer() {
        this.batches = new ArrayList<>();
    }

    public void render() {
        for (RenderBatch batch : batches) {
            batch.render();
        }
    }

    public void add(Sprite spr) {
        if(spr != null) {
            _add( spr);
        }

    }

    public void remove(Sprite spr) {
        if ((spr != null))
            for (RenderBatch batch : batches)
                if (batch.removeSprite(spr))
                    return;
    }

    private void _add(Sprite spr) {
        for (int i = batches.size() - 1; i >= 0; --i) {
            if(batches.get(i).addSprite(spr))
                return;
        }
        RenderBatch newBatch = AssetPool.getBatchOf(spr.getShader());

        newBatch.start();
        batches.add(newBatch);
        newBatch.addSprite(spr);
    }
}

