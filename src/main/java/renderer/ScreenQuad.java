package renderer;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import jade.Window;
import org.lwjgl.system.MemoryUtil;
import util.AssetPool;

public class ScreenQuad {
    private final int vaoID;
    private final int vboID;

    public static Shader shader;

    // The screen quad vertices are in NDC, along with their texture coordinates
    private static final float[] VERTICES = {
            // Position         // Texture Coords
            -1.0f,  1.0f,       0.0f, 1.0f,
            -1.0f, -1.0f,       0.0f, 0.0f,
            1.0f, -1.0f,       1.0f, 0.0f,

            -1.0f,  1.0f,       0.0f, 1.0f,
            1.0f, -1.0f,       1.0f, 0.0f,
            1.0f,  1.0f,       1.0f, 1.0f
    };

    public ScreenQuad() {
        shader = AssetPool.getShader("assets/shaders/screen.glsl");

        FloatBuffer vertexBuffer = null;
        try {
            // Allocate the buffer for the vertices
            vertexBuffer = MemoryUtil.memAllocFloat(VERTICES.length);
            vertexBuffer.put(VERTICES).flip();

            // Create and bind a VAO
            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            // Create a VBO and upload the vertex data
            vboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

            // Set the vertex attribute pointers
            // Position attribute (location 0)
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            // Texture coordinate attribute (location 1)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
            glEnableVertexAttribArray(1);

            // Unbind the VBO and VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (vertexBuffer != null) {
                MemoryUtil.memFree(vertexBuffer);
            }
        }
    }

    public void draw() {
        shader.uploadTexture("screenTexture", 0);

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteVertexArrays(vaoID);
        glDeleteBuffers(vboID);
    }
}
