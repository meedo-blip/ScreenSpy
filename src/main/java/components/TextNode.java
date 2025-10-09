package components;

import font.MyFont;
import jade.Constants;
import jade.Transform;
import jade.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.Objects;

public abstract class TextNode extends QuadSprite {

    protected String text;
    public MyFont font;
    protected int fontsize;
    protected Vector4f textColor = Constants.WHITE;

    public TextNode(MyFont font, String text, int fontsize, Vector4f color)
    {
        this.text = text;
        this.font = font;
        this.fontsize = fontsize;
        textColor = color;
        color = Constants.INVISIBLE;
    }

    protected void makeFontSprites(int startIndex) {
        int width = text.length();
        if(width == 0) return;
        int height = (int) Math.floor(text.length() / width) + 1;

        float halfW = (float) text.length() / 2;

        for (int i = startIndex; i < text.length(); i++) {
            Window.getScene().addSpriteObjectToScene(
                    new FontSprite(font.texId, font.getCharTexCoords(text.charAt(i)), textColor)
                            .setTransform(new Transform(new Vector2f(((i % width) - halfW) * fontsize, (i / width) * fontsize), -1, new Vector2f(fontsize, fontsize)))
                            .setName("" + text.charAt(i)), this);
        }
    }

    public void setText(String text) {
        if(text.equals(this.text)) return;
        this.text = text;
        Window.getScene().removeChildrenOf(this);
        makeFontSprites(0);
    }

    @Override
    public void start() {
        super.start();
        makeFontSprites(0);
        changed = false;
    }
}
