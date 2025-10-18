package components;

import jade.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import renderer.Shader;

public class StaticBlock extends QuadSprite {

    public StaticBlock(Shader shader, Vector4f color) {
        this.shader = shader;
        this.color = color;
        this.texId = -1;
    }

    public StaticBlock(Shader shader, int texture) {
        this.shader = shader;
        this.texId = texture;
        this.color = new Vector4f(1,0,0,1);
    }

    @Override
    public void start() {
        super.start();
        changed = true;


    }

    @Override
    public void update(float dt) {
    }
}
