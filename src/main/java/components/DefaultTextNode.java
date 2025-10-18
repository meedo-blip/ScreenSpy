package components;

import font.MyFont;
import org.joml.Vector4f;

public class DefaultTextNode extends TextNode {

    public DefaultTextNode(MyFont font, String text, int fontsize, Vector4f transform) {
        super(font, text, fontsize, transform);
    }

    @Override
    public void update(float dt) {
    }
}
