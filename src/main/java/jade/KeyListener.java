package jade;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {
    private static final boolean[] keyPressed = new boolean[350];
    private static final boolean[] justKeyReleased = new boolean[350];

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            keyPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            justKeyReleased[key] = true;
            keyPressed[key] = false;
        }
    }

    public static boolean isKeyPressed(int keyCode) {
        return keyPressed[keyCode];
    }

    public static boolean isKeyClicked(int keyCode) {
        boolean result = justKeyReleased[keyCode];
        justKeyReleased[keyCode] = false;
        return result;
    }
}
