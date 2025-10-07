package renderer;

import components.QuadSprite;
import jade.Transform;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;

public class DefaultBatch extends QuadBatch {
    // Vertex
    // =======
    // Pos              Scale               Color                  Tex coords      tex id
    // float, float     float, float,       float, float, float    float, float    float
    private final int
            POS_SIZE = 3,
            SCALE_SIZE = 2,
            COLOR_SIZE = 4,
            TEX_COORDS_SIZE = 2,
            TEX_ID_SIZE = 1;

    protected final int
            POS_OFFSET = 0,
            SCALE_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES),
            COLOR_OFFSET = SCALE_OFFSET + (SCALE_SIZE * Float.BYTES),
            TEX_COORDS_OFFSET = COLOR_OFFSET + (COLOR_SIZE * Float.BYTES),
            TEX_ID_OFFSET = TEX_COORDS_OFFSET + (TEX_COORDS_SIZE * Float.BYTES),
            VERTEX_SIZE = 12,
            VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    public DefaultBatch(Shader shader) {
        super(shader, 11);
    }

    @Override
    protected void initAttribPointers() {
        // Info on the vertex attribute pointers
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);

        glVertexAttribPointer(1, SCALE_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, SCALE_OFFSET);

        glVertexAttribPointer(2, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);

        glVertexAttribPointer(3, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);

        glVertexAttribPointer(4, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);

    }

    @Override
    protected void enableVertexAttribs() {
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
    }

    @Override
    protected void disableVertexAttribs() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
    }

    @Override
    protected void loadVertexProperties(int index) {
        QuadSprite sprite = (QuadSprite) this.sprites.get(index);

        // Find offset within vertices array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;

        Transform transform = sprite.transform;
        Vector4f color = sprite.getColor();
        float[] uv = sprite.getTexCoords();
        // texID is the tex position in textures
        // 0 means no texture
        float tex_id = textures.indexOf(sprite.getTexId()) + 1;
        float halfX = transform.scale.x / 2;
        float halfY = transform.scale.y / 2;

        for(int i = 0; i < 4; i++) {

            // Top right     0
            // Top left      1
            // Bottom left   2
            // Bottom right  3

            // Load position
            vertices[offset] = transform.position.x;
            vertices[offset + 1] = transform.position.y;
            vertices[offset + 2] = transform.z;

            // Load scale
            vertices[offset + 3] = halfX;
            vertices[offset + 4] = halfY;

            // Load color
            vertices[offset + 5] = color.x;
            vertices[offset + 6] = color.y;
            vertices[offset + 7] = color.z;
            vertices[offset + 8] = color.w;

            // Load Texture coords
            vertices[offset + 9] = uv[i << 1];
            vertices[offset + 10] = uv[(i << 1) + 1];

            // Load texture id
            vertices[offset + 11] = tex_id;

            offset += VERTEX_SIZE;
        }
    }
}
