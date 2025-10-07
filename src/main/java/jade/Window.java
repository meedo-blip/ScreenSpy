package jade;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.*;
import renderer.FboRenderer;
import renderer.ScreenQuad;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL20.GL_MAX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;
import static util.Time.dt;

public class Window {

    private String title;

    // Location of window in the memory
    private long glfwWindow;

    private static Window window = null;

    public int window_w = 1920, window_h = 1080;

    private FboRenderer fboRenderer;
    private static IntBuffer bufferW, bufferH;
    private static Scene currentScene;
    private long audioDevice;
    private long audioContext;

    private long ticks = 0;

    private Window() {
        bufferH = BufferUtils.createIntBuffer(1);
        bufferW = BufferUtils.createIntBuffer(1);
    }

    public static void changeScene(Scene newScene) {
        currentScene = newScene;
        currentScene.init();
        currentScene.start();
    }

    public static Window get(String title) {
        if (Window.window == null) {
            Window.window = new Window();
            Window.window.title = title;
        }

        return Window.window;
    }

    public static long getTicks() {return window.ticks; }

    public static Scene getScene() {
        return currentScene;
    }

    public static int getWidth() {
        return bufferW.get(0);
    }

    public static int getHeight() {
        return bufferH.get(0);
    }

    public static FboRenderer getFbo() {
        return window.fboRenderer;
    }

    public void run(Scene scene) {
        System.out.println("Hello LWJGL"+ Version.getVersion() +"!");

        init();
        changeScene(scene);
        loop();

        // Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Destroy the audio context
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);

        // Terminate glfw and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        // Setup error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // invisible
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // can be resized
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE); // set fullscreen to true

        // Create the window
        glfwWindow = glfwCreateWindow(this.window_w, this.window_h, this.title, NULL, NULL);

        if (glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindow);

        // Enable vsync, this locks your frame rate to the refresh rate of the user's monitor
        glfwSwapInterval(0);

        // Make the window visible
        glfwShowWindow(glfwWindow);

        // This line is critical for LWJGL's interoperation with GLFW
        GL.createCapabilities();

        // Initialize audio device
        // Initialize the audio device
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            assert false : "Audio library not supported.";
        }

        int[] texture_units = new int[1];
        glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, texture_units);

        // Get window size through mutable buffer
        glfwGetWindowSize(window.glfwWindow, bufferW, bufferH);
        window_w = bufferW.get(0);
        window_h = bufferH.get(0);
        fboRenderer = new FboRenderer(window_w, window_h);

        System.out.println("Your Gpu supports upto " + texture_units[0] + " textures per batch.");
    }

	public void loop() {

        ScreenQuad screenQuad = new ScreenQuad();
        float startTime = (float) glfwGetTime();
        float endTime;
        dt = 0.0f;

        // Set clear color
        glClearColor(1f, 1f, 1f, 1.0f);

        while (!glfwWindowShouldClose(glfwWindow)) {
            // Poll events
            glfwPollEvents();

            fboRenderer.bind();

            glClear(GL_COLOR_BUFFER_BIT);

            currentScene.update(dt);

            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
                System.err.println("ERROR::FRAMEBUFFER:: Framebuffer is not complete!");

            fboRenderer.unbind();

            glViewport(0, 0, window_w, window_h);

            ScreenQuad.shader.use();
            glDisable(GL_DEPTH_TEST);
            glBindTexture(GL_TEXTURE_2D, fboRenderer.getTexture());

            screenQuad.draw();

            glfwSwapBuffers(glfwWindow);

            endTime = (float) glfwGetTime();
            dt = endTime - startTime;
            startTime = endTime;

            if(currentScene.fixedDT > dt) {
                try {
                    Thread.sleep((long) ((currentScene.fixedDT - dt) * 1000));
                } catch (InterruptedException ignored) {
                }
            }

            ++ticks;
        }
    }
}
