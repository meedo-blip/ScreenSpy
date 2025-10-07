package renderer;

import components.QuadSprite;
import components.Sprite;
import jade.Constants;
import jade.Window;

import java.util.ArrayList;
import java.util.List;

import static jade.Constants.MAX_BATCH_SIZE;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL20.*;

public abstract class QuadBatch extends RenderBatch {
    protected List<Integer> textures;
    protected int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};

    public QuadBatch(Shader shader, int vertSize) {
        this.shader = shader != null ? shader : Constants.DEFAULT_SH;
        vertexSize = vertSize;
    }

    @Override
    public void start() {
        sprites = new ArrayList<>(MAX_BATCH_SIZE);
        textures = new ArrayList<>(7);

        vertices = new float[4 * MAX_BATCH_SIZE * vertexSize];

        // Generate and bind a Vertex Array Object
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Allocate space for vertices
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);

        // Buffer types: Array                    size in bytes               vertices can change
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Create and upload indices buffer
        // e.g. 2, 1, 3,
        //      3, 0, 1
        int eboID = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        initAttribPointers();
    }

    protected abstract void initAttribPointers();

    @Override
    public boolean addSprite(Sprite sprite) {
        if(sprite instanceof QuadSprite) {
            QuadSprite spr = (QuadSprite) sprite;
            if (hasRoom() && shader == spr.getShader()) {
                if (spr.getTexId() == -1 || hasTextureRoom() || textures.contains(spr.getTexId())) {
                    sprites.add(sprite);
                    numSprites++;

                    if (spr.texId != -1)
                        if (!textures.contains(spr.texId))
                            textures.add(spr.texId);

                    loadVertexProperties(numSprites - 1);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render() {

        byte presentTextures = 0;
        int idx;
        // Update vertices
        for (int i = 0; i < numSprites; i++) {
            QuadSprite spr = (QuadSprite) sprites.get(i);
            if(spr.isChanged()) {
                idx = textures.indexOf(spr.texId);
                if(idx == -1){
                    if(hasTextureRoom()){
                        textures.add(spr.texId);
                        //presentTextures |= (byte) (1 << (textures.size() - 1));
                    } else {
                        this.sprites.remove(spr);
                        numSprites--;
                        //byte temp = (byte) (presentTextures &  ~(byte) (Math.pow(2,idx) - 1));
                        //presentTextures = (byte)((temp >> 1) | (presentTextures & (byte) (Math.pow(2,idx) - 1)));

                        Window.getScene().getRenderer().add(spr);
                        continue;
                    }
                } else {
                    //presentTextures |= (byte) (1 << idx);
                }

                loadVertexProperties(i);
            }
        }

        //System.out.println("Present textures: " + Integer.toBinaryString(presentTextures));
        // For now we will rebuffer all data every frame
        glBindVertexArray(vaoID);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);


        // Use shader
        shader.use();

        // Upload matrices and uniforms
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        uploadUniforms();

        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1);
            glBindTexture(GL_TEXTURE_2D, textures.get(i));
        }

        shader.uploadIntArray("uTextures", texSlots);

        glBindVertexArray(vaoID);
        enableVertexAttribs();

        glDrawElements(GL_TRIANGLES, numSprites * 6, GL_UNSIGNED_INT, 0);

        disableVertexAttribs();
        glBindVertexArray(0);

        for (int i = 0; i < textures.size(); i++) {
            glActiveTexture(textures.get(i));
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        glActiveTexture(GL_TEXTURE0);
        shader.detach();
    }

    protected abstract void enableVertexAttribs();

    protected abstract void disableVertexAttribs();

    protected void uploadUniforms() {}

    protected boolean hasTextureRoom() {
        return textures.size() < 7;
    }

    protected boolean hasTexture(int tex) {
        return textures.contains(tex);
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 per triangle)
        int[] elements = new int[6 * MAX_BATCH_SIZE];

        // elements array is mutable
        for(int i=0; i < MAX_BATCH_SIZE; i++)
            loadElementIndices(elements, i);

        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // Element 1                  Element 2
        // 3, 2, 0, 0, 2, 1           7, 6, 4, 4, 6, 5

        // Triangle 1
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset;

        // Triangle 2
        elements[offsetArrayIndex + 3] = offset;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

}
