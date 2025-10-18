package jade;

import components.DefaultTextNode;
import components.Sprite;
import components.TextNode;
import font.MyFont;
import org.joml.Vector2f;
import org.joml.Vector4f;
import renderer.Renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Scene {

    public float fixedDT = 0f; // in seconds

    protected final Renderer renderer = new Renderer();
	protected Camera camera = new Camera(new Vector2f(-1,1));
    protected final List<Sprite> gameSprites = new ArrayList<>();
    protected final List<Integer> gameParents = new ArrayList<>();

    private int spriteId = 1;
    private boolean isRunning = false;
    private int ticks = 0;
    private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

    public Scene() {}

    public void init() {}

    public void start() {

        for(int i = 0; i < gameSprites.size(); i++) {
            Sprite spr = gameSprites.get(i);
            spr.start();
            this.renderer.add(spr);
        }
        isRunning = true;
    }

    public Sprite addSprite(Sprite spr) {
        spr.id = spriteId++;
        gameSprites.add(spr);

        if(isRunning) {
            spr.start();
            this.renderer.add(spr);
        }

        return spr;
    }


    public Sprite addSprite(Sprite spr, Sprite parent) {
        if(parent == null)
            return addSprite(spr);

        spr.id = spriteId++;
        gameSprites.add(spr);
        addSpriteToParent(spr, parent);

        if(isRunning) {
            spr.start();
            this.renderer.add(spr);
        }

        return spr;
    }

    public TextNode makeText(MyFont font, String text, float x, float y, int fontsize, Vector4f color) {
        return makeText(font, text, x, y, fontsize, color, null);

    }

    public TextNode makeText(MyFont font, String text, float x, float y, int fontsize, Vector4f color, Sprite grandParent) {

        int width = text.length();
        int height = 1;

        return (TextNode) addSprite(new DefaultTextNode(font, text, fontsize, color).setTransform(new Transform(new Vector2f(x,y), -1, new Vector2f(fontsize * width, fontsize * height))), grandParent);
    }

    public Sprite getSprite(String name) {
        for (Sprite spr : gameSprites)
            if(spr.getName().equals(name))
                return spr;

        return null;
    }

    public void removeSprite(Sprite spr) {
        if(spr == null) return;
        int loc2 = gameParents.indexOf(-spr.id);
        if (loc2 != -1) {
            loc2++;
            while (!(gameParents.get(loc2) < 0)) {
                removeSprite(getSpriteById(gameParents.remove(loc2)));
                if(loc2 == gameParents.size())
                    break;
            }

            gameParents.remove(loc2 - 1);
        }

        gameSprites.remove(spr);
        renderer.remove(spr);
    }

    public void pollEvents() {
        for(int i = 0; i < queue.size(); i++) {
            queue.remove().run();
        }
    }

    public void queueEvent(Runnable r) {
        queue.add(r);
    }

    public void update(float dt) {
        for(int i = 0; i < gameSprites.size(); i++) {
            Sprite spr = gameSprites.get(i);
            spr.update(dt);
        }


        renderer.render();

        ticks++;
    }



    public void dispose() {}

    public int getTicks() { return ticks; }

    public Renderer getRenderer() { return this.renderer; }

    public Camera camera() { return this.camera; }

    public Sprite getSpriteById(int id){
        int pos = Math.min(id, gameSprites.size() - 1);
        int i = pos;
        for (; i >= 0; i--) {
            if(gameSprites.get(i).id == id)
                return gameSprites.get(i);
        }
        for (i = pos + 1; i < gameSprites.size(); i++) {
            if(gameSprites.get(i).id == id)
                return gameSprites.get(i);
        }

        return null;
    }

    public Sprite getSpriteByName(String name) {
        for (Sprite spr : gameSprites)
            if(spr.name.equals(name))
                return spr;

        return null;
    }

    public Sprite getParentOf(Sprite sprite) {
        int loc = gameParents.indexOf(sprite.id);
        if(loc == -1) return null;

        while(loc >= 0) {
            loc--;
            if(gameParents.get(loc) < 0) {
                return getSpriteById(-gameParents.get(loc));
            }
        }

        return null;
    }

    private void addSpriteToParent(Sprite spr, Sprite parent) {
        int loc2 = gameParents.indexOf(-parent.id);
        if(!gameSprites.contains(parent)) {
            gameParents.add(-addSprite(parent).id);
            loc2 = gameParents.size();
        } else if (loc2 == -1) {
            gameParents.add(-parent.id);
            loc2 = gameParents.size();
        } else {
            loc2++;
        }
        gameParents.add(loc2, spr.id);
        spr.parent = parent;
    }

    public void removeChildrenOf(Sprite parent) {
        int loc = gameParents.indexOf(-parent.id);

        if(loc != -1) {
            gameParents.remove(loc);
            while(gameParents.get(loc) >= 0){

                if(gameParents.size() - 1 == loc) {
                    removeSprite(getSpriteById(gameParents.remove(loc)));
                    break;
                }

                removeSprite(getSpriteById(gameParents.remove(loc)));
            }
        } else {
            System.out.println("Sprite " + parent.name + " has no children!");
        }
    }

    public List<Sprite> getChildrenOf(Sprite spr) {
        int loc = gameParents.indexOf(-spr.id);
        if(loc == -1) return List.of();

        List<Sprite> list = new ArrayList<>(8);

        loc++;
        while(loc < gameParents.size() && gameParents.get(loc) > 0){
            Sprite sprite = getSpriteById(gameParents.get(loc));
            loc++;

            if(sprite == null) { continue;}
            list.add(getSpriteById(sprite.id));
        }

        return list;
    }
}
