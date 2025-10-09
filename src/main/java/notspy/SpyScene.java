package notspy;

import components.StaticBlock;
import components.TextNode;
import jade.Scene;
import jade.Transform;
import jade.Window;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static jade.Constants.ARIAL_FONT;
import static jade.Constants.DEFAULT_SH;

public class SpyScene extends Scene {

    private RecieverServer server;

    private StaticBlock block;
    private TextNode infoText;
    private static int blockWidth = 1280;  // scaled resolution
    private static int blockHeight = blockWidth * 9 / 16;

    // Thread-safe queue for raw bytes from network
    private static final ConcurrentLinkedQueue<byte[]> receivedQueue = new ConcurrentLinkedQueue<>();

    private int textureId = -1; // reusable texture

    // Decompress RLE RGBA array
    public static byte[] decompress(byte[] compressed) {
        List<Byte> out = new ArrayList<>();
        int i = 0;
        while (i < compressed.length) {
            int count = Byte.toUnsignedInt(compressed[i++]);
            byte r = compressed[i++];
            byte g = compressed[i++];
            byte b = compressed[i++];
            byte a = compressed[i++];

            for (int j = 0; j < count; j++) {
                out.add(r);
                out.add(g);
                out.add(b);
                out.add(a);
            }
        }

        byte[] result = new byte[out.size()];
        for (int j = 0; j < out.size(); j++) result[j] = out.get(j);
        return result;
    }

    public static byte[] decompress_RGB(byte[] compressed) {
        List<Byte> out = new ArrayList<>();
        int i = 0;
        while (i < compressed.length) {
            int count = Byte.toUnsignedInt(compressed[i++]);
            byte r = compressed[i++];
            byte g = compressed[i++];
            byte b = compressed[i++];

            for (int j = 0; j < count; j++) {
                out.add(r);
                out.add(g);
                out.add(b);
            }
        }

        byte[] result = new byte[out.size()];
        for (int j = 0; j < out.size(); j++) result[j] = out.get(j);
        return result;
    }

    @Override
    public void init() {
        // Initialize block
        block = (StaticBlock) new StaticBlock(DEFAULT_SH, new org.joml.Vector4f(1,1,1,1))
                .setTransform(new Transform(new Vector2f(0,0), -1, new Vector2f(blockWidth, blockHeight)));
        addSpriteObjectToScene(block);


        try {
            server = new RecieverServer(5000, receivedQueue);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        infoText = makeText(ARIAL_FONT, "Waiting for connection...", 0, 0, 24, new Vector4f(0.5f,0.5f,1,1));
    }

    @Override
    public void dispose() {
        server.thread.interrupt();
    }
    @Override
    public void update(float dt) {
        super.update(dt);

        // Consume queued data safely on render thread
        byte[] data = receivedQueue.poll();
        receivedQueue.clear();

        if(server.isClientConnected()) infoText.setText("");
        else {
            infoText.setText("Waiting for connection...");
            block.texId = -1;
        }

        if (data == null) {
            return; // No new frame
        }

        data = decompress_RGB(data);

        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.put(data);
        buffer.flip();

        if (block.texId == -1) {
            // Create texture once
            block.texId = Utils.generateTexture(buffer, blockWidth, blockHeight, 3);
        } else {
            // Update existing texture
            Utils.updateTexture(block.texId, buffer, blockWidth, blockHeight, 3);
        }
    }
}
