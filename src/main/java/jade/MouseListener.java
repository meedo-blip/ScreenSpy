package jade;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {
    private static MouseListener instance = new MouseListener();
    private double scrollX, scrollY;
    private double xPos, yPos, lastX, lastY;
    private final boolean[] mouseButtonPressed = new boolean[5];
    private boolean isDragging, justScrolled;

    private MouseListener() {
        this.scrollX = 0.0;
        this.scrollY = 0.0;
        this.xPos = 0.0;
        this.yPos = 0.0;
        this.lastX = 0.0;
        this.lastY = 0.0;
    }

    public static MouseListener get() {
        return MouseListener.instance;
    }

    public static void mousePosCallback(long window, double x, double y) {
        instance.lastX = instance.xPos;
        instance.lastY = instance.yPos;
        instance.xPos = x;
        instance.yPos = y;

        for (int i = 0; i < instance.mouseButtonPressed.length; i++) {
            if (instance.mouseButtonPressed[i]) {
                instance.isDragging = true;
                break;
            }
        }
    }

    public static void mouseButtonCallback(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS) {
            if (button < instance.mouseButtonPressed.length) {
                instance.mouseButtonPressed[button] = true;
            }
        } else if (action == GLFW_RELEASE) {
            if (button < instance.mouseButtonPressed.length) {
                instance.mouseButtonPressed[button] = false;
                instance.isDragging = false;
            }
        }

    }

    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        instance.scrollX = xOffset;
        instance.scrollY = yOffset;
    }

    public static void endFrame() {
        instance.lastX = instance.xPos;
        instance.lastY = instance.yPos;
        instance.scrollX = 0.0;
        instance.scrollY = 0.0;
    }

    public static float getX() {
        return (float) instance.xPos;
    }

    public static float getY() {
        return (float) instance.yPos;
    }

    // Delta x, change in x from last position
    public static float getDx() { return (float) (instance.lastX - instance.xPos); }

    // Delta y, change in y from last position
    public static float getDy() {
        return (float) (instance.lastY - instance.yPos);
    }

    public static float getScrollX() {
        return (float) instance.scrollX;
    }

    public static float getScrollY() {
        return (float) instance.scrollY;
    }

    public static boolean isDragging() {
        return instance.isDragging;
    }

    public static boolean justScrolled() {
        boolean result = instance.justScrolled;
        instance.justScrolled = false;
        return result;
    }

    public static boolean mouseButtonDown(int button) {
        if (button < instance.mouseButtonPressed.length) {
            return instance.mouseButtonPressed[button];
        }
        else return false;
    }

}
