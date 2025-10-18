package components;

import font.MyFont;
import jade.Constants;
import jade.Transform;
import jade.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;

public abstract class TextNode extends QuadSprite {

    public MyFont font;

    protected String text;
    protected int fontsize;
    protected Vector4f textColor = Constants.WHITE;
    protected boolean hidden = false;

    public TextNode(MyFont font, String text, int fontsize, Vector4f color) {
        this.text = text;
        this.font = font;
        this.fontsize = fontsize;
        textColor = color;
        this.shader = Constants.DEFAULT_SH;
        this.color = Constants.INVISIBLE;
    }

    protected void makeFontSprites(int startIndex) {
        int width = text.length();
        if (width == 0) return;

        float halfW = (float) text.length() / 2;

        for (int i = startIndex; i < text.length(); i++) {
            Window.getScene().addSprite(
                    new FontSprite(font.texId, font.getCharTexCoords(text.charAt(i)), textColor)
                            .setTransform(getCharTransform(i))
                            .setName("" + text.charAt(i)), this);
        }
    }

    protected Transform getCharTransform(int i) {
        int width = text.length();
        if (width == 0) return new Transform();

        float halfW = (float) text.length() / 2;
        return new Transform(new Vector2f(((i % width) - halfW) * fontsize, (i / width) * fontsize), -0.5f, new Vector2f(fontsize, fontsize));  }

    public void setText(String text) {
        if (text.equals(this.text)) return;
        this.text = text;

        List<Sprite> children = Window.getScene().getChildrenOf(this);
        int width = text.length();
        float halfW = (float) text.length() / 2;

        for (int i = 0; i < text.length(); i++) {
            if(i == children.size()) {
                makeFontSprites(i);
                return;
            }
            FontSprite spr = (FontSprite) children.get(i);
            if(spr == null) { i--; continue; }
            spr.texCoords = font.getCharTexCoords(text.charAt(i));
            spr.color = hidden ? Constants.INVISIBLE : textColor;
            spr.x(((i % width) - halfW) * fontsize);
        }
        // Remove extra characters
        for (int i = text.length(); i < children.size(); i++) {
            Window.getScene().removeSprite(children.get(i));
        }
    }

    public void hide(boolean hidden) {
        if (this.hidden == hidden) return;
        this.hidden = hidden;
        for (Sprite spr : Window.getScene().getChildrenOf(this))
            spr.color = hidden ? Constants.INVISIBLE : textColor;
    }

    @Override
    public void start() {
        super.start();
        transform.scale.x = 0;
        transform.scale.y = 0;
        makeFontSprites(0);
        changed = true;
    }
}
